package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StringLibTest {

    private val RAM_SIZE = 2 * 1024 * 1024

    private fun makeTable(): SyscallTable {
        val ram = VirtualRam(RAM_SIZE)
        val stack = OperandStack()
        return SyscallTable(ram, stack, System.out, System.`in`, heapStart = 4, heapSize = RAM_SIZE / 2)
    }

    private fun str(t: SyscallTable, addr: Int, s: String): Int {
        t.ram.writeString(addr, s)
        return addr
    }

    @Test
    fun `strlen returns length`() {
        val t = makeTable()
        str(t, 1000, "hello")
        t.stack.push(1000)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRLEN)
        assertEquals(5, t.stack.pop())
    }

    @Test
    fun `strcpy copies string and returns dst`() {
        val t = makeTable()
        str(t, 1000, "world")
        t.stack.push(2000) // dst (deepest)
        t.stack.push(1000) // src (top)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRCPY)
        assertEquals(2000, t.stack.pop())
        assertEquals("world", t.ram.readString(2000))
    }

    @Test
    fun `strcat appends and returns dst`() {
        val t = makeTable()
        str(t, 1000, "hello")
        str(t, 2000, " world")
        t.stack.push(1000) // dst
        t.stack.push(2000) // src
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRCAT)
        assertEquals(1000, t.stack.pop())
        assertEquals("hello world", t.ram.readString(1000))
    }

    @Test
    fun `strcmp returns 0 for equal strings`() {
        val t = makeTable()
        str(t, 1000, "abc")
        str(t, 2000, "abc")
        t.stack.push(1000)
        t.stack.push(2000)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRCMP)
        assertEquals(0, t.stack.pop())
    }

    @Test
    fun `strcmp returns negative for lesser string`() {
        val t = makeTable()
        str(t, 1000, "abc")
        str(t, 2000, "abd")
        t.stack.push(1000)
        t.stack.push(2000)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRCMP)
        assertTrue(t.stack.pop() < 0)
    }

    @Test
    fun `strchr finds character`() {
        val t = makeTable()
        str(t, 1000, "hello")
        t.stack.push(1000)       // s
        t.stack.push('l'.code)   // c
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRCHR)
        assertEquals(1002, t.stack.pop()) // 'l' is at index 2
    }

    @Test
    fun `strchr returns 0 when not found`() {
        val t = makeTable()
        str(t, 1000, "hello")
        t.stack.push(1000)
        t.stack.push('z'.code)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRCHR)
        assertEquals(0, t.stack.pop())
    }

    @Test
    fun `strstr finds substring`() {
        val t = makeTable()
        str(t, 1000, "hello world")
        str(t, 2000, "world")
        t.stack.push(1000) // haystack
        t.stack.push(2000) // needle
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRSTR)
        assertEquals(1006, t.stack.pop()) // "world" starts at index 6
    }

    @Test
    fun `strstr returns 0 when not found`() {
        val t = makeTable()
        str(t, 1000, "hello")
        str(t, 2000, "xyz")
        t.stack.push(1000)
        t.stack.push(2000)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRSTR)
        assertEquals(0, t.stack.pop())
    }

    @Test
    fun `memcpy copies bytes and returns dst`() {
        val t = makeTable()
        str(t, 1000, "abcde")
        t.stack.push(2000) // dst
        t.stack.push(1000) // src
        t.stack.push(5)    // n
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.MEMCPY)
        assertEquals(2000, t.stack.pop())
        assertEquals("abcde", t.ram.readString(2000))
    }

    @Test
    fun `memmove handles overlap`() {
        val t = makeTable()
        // Write "abcde" at 1000, then memmove(1002, 1000, 3) → "ababcde" at 1000
        str(t, 1000, "abcde")
        t.stack.push(1002) // dst (overlaps src)
        t.stack.push(1000) // src
        t.stack.push(3)    // n
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.MEMMOVE)
        assertEquals(1002, t.stack.pop())
        assertEquals("abc", t.ram.readString(1002))
    }

    @Test
    fun `memset fills bytes`() {
        val t = makeTable()
        t.stack.push(1000) // ptr
        t.stack.push('X'.code) // c
        t.stack.push(5)    // n
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.MEMSET)
        assertEquals(1000, t.stack.pop())
        for (i in 0 until 5) assertEquals('X'.code.toByte(), t.ram.readByte(1000 + i))
    }

    @Test
    fun `memcmp returns 0 for equal regions`() {
        val t = makeTable()
        str(t, 1000, "abcd")
        str(t, 2000, "abcd")
        t.stack.push(1000)
        t.stack.push(2000)
        t.stack.push(4)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.MEMCMP)
        assertEquals(0, t.stack.pop())
    }

    @Test
    fun `strdup allocates copy`() {
        val t = makeTable()
        str(t, 1000, "hello")
        t.stack.push(1000)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRDUP)
        val ptr = t.stack.pop()
        assertNotEquals(0, ptr)
        assertNotEquals(1000, ptr)
        assertEquals("hello", t.ram.readString(ptr))
    }

    @Test
    fun `strncpy pads with nulls`() {
        val t = makeTable()
        str(t, 1000, "hi")
        t.stack.push(2000) // dst
        t.stack.push(1000) // src
        t.stack.push(5)    // n
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRNCPY)
        assertEquals(2000, t.stack.pop())
        assertEquals('h'.code.toByte(), t.ram.readByte(2000))
        assertEquals('i'.code.toByte(), t.ram.readByte(2001))
        assertEquals(0.toByte(), t.ram.readByte(2002))
        assertEquals(0.toByte(), t.ram.readByte(2003))
        assertEquals(0.toByte(), t.ram.readByte(2004))
    }

    @Test
    fun `strncmp compares first n chars`() {
        val t = makeTable()
        str(t, 1000, "abcXX")
        str(t, 2000, "abcYY")
        t.stack.push(1000)
        t.stack.push(2000)
        t.stack.push(3)    // compare only first 3
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.STRNCMP)
        assertEquals(0, t.stack.pop())
    }
}
