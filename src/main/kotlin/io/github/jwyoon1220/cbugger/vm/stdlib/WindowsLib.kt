package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * VM implementation of a subset of windows.h functions.
 *
 * This simulates Windows API behavior on any host OS.
 * Non-portable functions are approximated using Java's standard library.
 */
internal class WindowsLib(
    private val ram: VirtualRam,
    private val stack: OperandStack
) {
    private var lastError: Int = 0
    private val vmStartNanos: Long = System.nanoTime()

    /** GetLastError() — returns the last error code */
    fun getLastError() {
        stack.push(lastError)
    }

    /** SetLastError(code) — sets the last error code */
    fun setLastError() {
        lastError = stack.pop()
    }

    /** Sleep(ms) — suspends the VM thread for [ms] milliseconds */
    fun sleep() {
        val ms = stack.pop()
        if (ms > 0) {
            try { Thread.sleep(ms.toLong()) } catch (_: InterruptedException) { Thread.currentThread().interrupt() }
        }
    }

    /**
     * GetTickCount() — milliseconds elapsed since VM started.
     * Wraps at ~49.7 days (DWORD overflow).
     */
    fun getTickCount() {
        val ms = (System.nanoTime() - vmStartNanos) / 1_000_000L
        stack.push((ms and 0xFFFFFFFFL).toInt())
    }

    /** ExitProcess(code) — terminates the VM */
    fun exitProcess() {
        val code = stack.pop()
        throw VmExitException(code)
    }

    /**
     * GetSystemTime(pSYSTEMTIME) — writes a SYSTEMTIME struct to RAM.
     *
     * SYSTEMTIME layout (8 WORDs = 16 bytes, each stored as Int in 4 bytes):
     *   +0  wYear, +4  wMonth, +8  wDayOfWeek, +12 wDay,
     *   +16 wHour, +20 wMinute, +24 wSecond, +28 wMilliseconds
     */
    fun getSystemTime() {
        val ptr = stack.pop()
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        ram.writeInt(ptr +  0, now.year)
        ram.writeInt(ptr +  4, now.monthValue)
        ram.writeInt(ptr +  8, now.dayOfWeek.value % 7) // Sunday=0
        ram.writeInt(ptr + 12, now.dayOfMonth)
        ram.writeInt(ptr + 16, now.hour)
        ram.writeInt(ptr + 20, now.minute)
        ram.writeInt(ptr + 24, now.second)
        ram.writeInt(ptr + 28, now.nano / 1_000_000)
    }

    /**
     * QueryPerformanceCounter(lpPerformanceCount) — writes a 64-bit counter.
     * Stores high 32 bits at ptr, low 32 bits at ptr+4.
     */
    fun queryPerformanceCounter() {
        val ptr = stack.pop()
        val ns = System.nanoTime()
        ram.writeInt(ptr,     (ns ushr 32).toInt())
        ram.writeInt(ptr + 4, ns.toInt())
        stack.push(1) // TRUE
    }

    /**
     * QueryPerformanceFrequency(lpFrequency) — writes the counter frequency.
     * Reports 1,000,000,000 Hz (nanosecond resolution).
     */
    fun queryPerformanceFrequency() {
        val ptr = stack.pop()
        val freq = 1_000_000_000L
        ram.writeInt(ptr,     (freq ushr 32).toInt())
        ram.writeInt(ptr + 4, freq.toInt())
        stack.push(1) // TRUE
    }

    /** OutputDebugStringA(lpOutputString) — writes the debug string to stderr */
    fun outputDebugString() {
        val ptr = stack.pop()
        val s = ram.readString(ptr)
        System.err.print(s)
    }
}
