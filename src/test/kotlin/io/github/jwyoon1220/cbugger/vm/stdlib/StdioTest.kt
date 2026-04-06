package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StdioTest {

    private val RAM_SIZE = 2 * 1024 * 1024

    private fun makeTable(out: PrintStream = System.out): SyscallTable {
        val ram = VirtualRam(RAM_SIZE)
        val stack = OperandStack()
        return SyscallTable(ram, stack, out, System.`in`, heapStart = 4, heapSize = RAM_SIZE / 2)
    }

    /** Writes a null-terminated string into RAM at [addr], returns [addr]. */
    private fun writeStr(t: SyscallTable, addr: Int, s: String): Int {
        t.ram.writeString(addr, s)
        return addr
    }

    @Test
    fun `putchar prints character and returns its value`() {
        val baos = ByteArrayOutputStream()
        val t = makeTable(PrintStream(baos))
        t.stack.push('A'.code)
        t.ram // touch to init
        io.github.jwyoon1220.cbugger.vm.isa.SyscallId.PUTCHAR.let { t.dispatch(it) }
        assertEquals("A", baos.toString())
        assertEquals('A'.code, t.stack.pop())
    }

    @Test
    fun `puts prints string with newline`() {
        val baos = ByteArrayOutputStream()
        val t = makeTable(PrintStream(baos))
        val addr = 10000
        writeStr(t, addr, "hello")
        t.stack.push(addr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.PUTS)
        assertEquals("hello\n", baos.toString())
        assertEquals(0, t.stack.pop())
    }

    @Test
    fun `printf formats integer`() {
        val baos = ByteArrayOutputStream()
        val t = makeTable(PrintStream(baos))
        val fmtAddr = 10000
        writeStr(t, fmtAddr, "val=%d")
        t.stack.push(42)       // arg (pushed first = deepest)
        t.stack.push(fmtAddr)  // fmt on top
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.PRINTF)
        assertEquals("val=42", baos.toString())
        assertEquals(6, t.stack.pop()) // length of "val=42"
    }

    @Test
    fun `printf formats string`() {
        val baos = ByteArrayOutputStream()
        val t = makeTable(PrintStream(baos))
        val fmtAddr = 10000
        val argAddr = 20000
        writeStr(t, fmtAddr, "hi %s!")
        writeStr(t, argAddr, "world")
        t.stack.push(argAddr)
        t.stack.push(fmtAddr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.PRINTF)
        assertEquals("hi world!", baos.toString())
    }

    @Test
    fun `printf handles percent-percent`() {
        val baos = ByteArrayOutputStream()
        val t = makeTable(PrintStream(baos))
        val fmtAddr = 10000
        writeStr(t, fmtAddr, "100%%")
        t.stack.push(fmtAddr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.PRINTF)
        assertEquals("100%", baos.toString())
    }

    @Test
    fun `sprintf writes to buffer`() {
        val t = makeTable()
        val bufAddr = 5000
        val fmtAddr = 6000
        writeStr(t, fmtAddr, "x=%d,y=%d")
        t.stack.push(3)       // y (deepest non-fmt arg)
        t.stack.push(7)       // wait - pushing left-to-right means first arg deepest
        // Convention: push args left-to-right (first arg pushed first = deepest)
        // sprintf(buf, fmt, x, y) => push buf, push x, push y, push fmt
        // Actually let's reset and do it properly:
        // Re-read convention: "buf deepest, fmt on top, varargs in between"
        // So stack (bottom->top): buf, x, y, fmt
        // We push: buf first, then x, then y, then fmt
        while (!t.stack.isEmpty()) t.stack.pop()

        t.stack.push(bufAddr) // buf (deepest)
        t.stack.push(1)       // x
        t.stack.push(2)       // y
        t.stack.push(fmtAddr) // fmt (top)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.SPRINTF)
        val result = t.ram.readString(bufAddr)
        assertEquals("x=1,y=2", result)
        assertEquals(7, t.stack.pop())
    }

    @Test
    fun `snprintf truncates at n-1`() {
        val t = makeTable()
        val bufAddr = 5000
        val fmtAddr = 6000
        writeStr(t, fmtAddr, "hello world")
        t.stack.push(bufAddr)  // buf (deepest)
        t.stack.push(6)        // n = 6 → truncate to 5 chars
        t.stack.push(fmtAddr)  // fmt (top, no args)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.SNPRINTF)
        val result = t.ram.readString(bufAddr)
        assertEquals("hello", result)
        assertEquals(11, t.stack.pop()) // total would-be length
    }
}
