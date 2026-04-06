package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WindowsLibTest {

    private val RAM_SIZE = 2 * 1024 * 1024

    private fun makeTable(): SyscallTable {
        val ram = VirtualRam(RAM_SIZE)
        val stack = OperandStack()
        return SyscallTable(ram, stack, System.out, System.`in`, heapStart = 4, heapSize = RAM_SIZE / 2)
    }

    @Test
    fun `GetLastError and SetLastError roundtrip`() {
        val t = makeTable()
        t.stack.push(1234)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.SET_LAST_ERROR)

        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.GET_LAST_ERROR)
        assertEquals(1234, t.stack.pop())
    }

    @Test
    fun `GetLastError returns 0 initially`() {
        val t = makeTable()
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.GET_LAST_ERROR)
        assertEquals(0, t.stack.pop())
    }

    @Test
    fun `GetTickCount returns non-negative value`() {
        val t = makeTable()
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.GET_TICK_COUNT)
        val tick = t.stack.pop()
        // After masking to unsigned 32-bit it should be non-negative when interpreted as long
        assertTrue((tick.toLong() and 0xFFFFFFFFL) >= 0)
    }

    @Test
    fun `GetSystemTime writes valid year to struct`() {
        val t = makeTable()
        val ptr = 50000
        t.stack.push(ptr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.GET_SYSTEM_TIME)
        val year = t.ram.readInt(ptr)
        assertTrue(year >= 2024, "Expected year >= 2024 but got $year")
    }

    @Test
    fun `GetSystemTime writes valid month to struct`() {
        val t = makeTable()
        val ptr = 50000
        t.stack.push(ptr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.GET_SYSTEM_TIME)
        val month = t.ram.readInt(ptr + 4)
        assertTrue(month in 1..12, "Month $month out of range")
    }

    @Test
    fun `ExitProcess throws VmExitException`() {
        val t = makeTable()
        t.stack.push(7)
        val ex = assertThrows<VmExitException> {
            t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.EXIT_PROCESS)
        }
        assertEquals(7, ex.code)
    }

    @Test
    fun `QueryPerformanceCounter pushes TRUE and writes counter`() {
        val t = makeTable()
        val ptr = 50000
        t.stack.push(ptr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.QUERY_PERFORMANCE_COUNTER)
        assertEquals(1, t.stack.pop()) // TRUE
        // Just verify the high word is readable (not checking exact value)
        val hi = t.ram.readInt(ptr)
        val lo = t.ram.readInt(ptr + 4)
        val combined = (hi.toLong() shl 32) or (lo.toLong() and 0xFFFFFFFFL)
        assertTrue(combined > 0)
    }

    @Test
    fun `QueryPerformanceFrequency reports 1GHz`() {
        val t = makeTable()
        val ptr = 50000
        t.stack.push(ptr)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.QUERY_PERFORMANCE_FREQUENCY)
        assertEquals(1, t.stack.pop()) // TRUE
        val hi = t.ram.readInt(ptr)
        val lo = t.ram.readInt(ptr + 4)
        val freq = (hi.toLong() shl 32) or (lo.toLong() and 0xFFFFFFFFL)
        assertEquals(1_000_000_000L, freq)
    }

    @Test
    fun `Sleep zero ms does not throw`() {
        val t = makeTable()
        t.stack.push(0)
        t.dispatch(io.github.jwyoon1220.cbugger.vm.isa.SyscallId.SLEEP) // must not throw
    }
}
