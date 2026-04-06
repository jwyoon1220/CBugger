package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StdlibTest {

    private val RAM_SIZE = 2 * 1024 * 1024

    private fun makeTable(): SyscallTable {
        val ram = VirtualRam(RAM_SIZE)
        val stack = OperandStack()
        return SyscallTable(ram, stack, System.out, System.`in`, heapStart = 4, heapSize = RAM_SIZE / 2)
    }

    @Test
    fun `malloc returns non-zero pointer`() {
        val t = makeTable()
        t.stack.push(64)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.MALLOC)
        assertNotEquals(0, t.stack.pop())
    }

    @Test
    fun `malloc and free and realloc`() {
        val t = makeTable()
        t.stack.push(32)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.MALLOC)
        val ptr = t.stack.pop()
        assertNotEquals(0, ptr)

        t.stack.push(ptr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.FREE)

        // After free, same block should be reused
        t.stack.push(32)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.MALLOC)
        val ptr2 = t.stack.pop()
        assertEquals(ptr, ptr2)
    }

    @Test
    fun `calloc returns zero-initialized block`() {
        val t = makeTable()
        t.stack.push(4)  // nmemb (deepest)
        t.stack.push(8)  // size (top)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.CALLOC)
        val ptr = t.stack.pop()
        assertNotEquals(0, ptr)
        for (i in 0 until 32) {
            assertEquals(0, t.ram.readByte(ptr + i).toInt())
        }
    }

    @Test
    fun `exit throws VmExitException with correct code`() {
        val t = makeTable()
        t.stack.push(42)
        val ex = assertThrows<VmExitException> {
            t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.EXIT)
        }
        assertEquals(42, ex.code)
    }

    @Test
    fun `atoi parses positive integer`() {
        val t = makeTable()
        val addr = 10000
        t.ram.writeString(addr, "123")
        t.stack.push(addr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.ATOI)
        assertEquals(123, t.stack.pop())
    }

    @Test
    fun `atoi parses negative integer`() {
        val t = makeTable()
        val addr = 10000
        t.ram.writeString(addr, "-456")
        t.stack.push(addr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.ATOI)
        assertEquals(-456, t.stack.pop())
    }

    @Test
    fun `atoi returns 0 for non-numeric`() {
        val t = makeTable()
        val addr = 10000
        t.ram.writeString(addr, "abc")
        t.stack.push(addr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.ATOI)
        assertEquals(0, t.stack.pop())
    }

    @Test
    fun `abs returns absolute value`() {
        val t = makeTable()
        t.stack.push(-99)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.ABS)
        assertEquals(99, t.stack.pop())

        t.stack.push(42)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.ABS)
        assertEquals(42, t.stack.pop())
    }

    @Test
    fun `rand returns non-negative value`() {
        val t = makeTable()
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.RAND)
        assertTrue(t.stack.pop() >= 0)
    }

    @Test
    fun `srand seeds deterministic sequence`() {
        val t = makeTable()
        t.stack.push(12345)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.SRAND)

        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.RAND)
        val r1 = t.stack.pop()

        // Re-seed with same value
        t.stack.push(12345)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.SRAND)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.RAND)
        val r2 = t.stack.pop()

        assertEquals(r1, r2)
    }
}
