package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.VmRuntimeException
import io.github.jwyoon1220.cbugger.vm.isa.SyscallId
import io.github.jwyoon1220.cbugger.vm.memory.HeapAllocator
import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import java.io.InputStream
import java.io.PrintStream

/**
 * Dispatcher for SYSCALL instructions.
 *
 * Delegates to the appropriate library handler based on the syscall ID.
 * I/O is fully configurable via [stdout]/[stdin] to enable unit testing.
 */
class SyscallTable(
    val ram: VirtualRam,
    val stack: OperandStack,
    stdout: PrintStream = System.out,
    stdin: InputStream = System.`in`,
    heapStart: Int = DEFAULT_HEAP_START,
    heapSize: Int = DEFAULT_HEAP_SIZE
) {
    companion object {
        const val DEFAULT_HEAP_START = 512 * 1024   // 512 KB into the 1 MB RAM
        const val DEFAULT_HEAP_SIZE  = 256 * 1024   // 256 KB heap
    }

    val heap = HeapAllocator(ram, heapStart, heapSize)

    private val stdio   = Stdio(ram, stack, heap, stdout, stdin)
    private val stdlib  = Stdlib(ram, stack, heap)
    private val strings = StringLib(ram, stack, heap)
    private val windows = WindowsLib(ram, stack)

    /**
     * Executes the syscall identified by [id].
     * @throws VmRuntimeException for unknown syscall IDs
     */
    fun dispatch(id: Int) {
        when (id) {
            // stdio
            SyscallId.PRINTF    -> stdio.printf()
            SyscallId.PUTCHAR   -> stdio.putchar()
            SyscallId.PUTS      -> stdio.puts()
            SyscallId.GETCHAR   -> stdio.getchar()
            SyscallId.SPRINTF   -> stdio.sprintf()
            SyscallId.SCANF     -> stdio.scanf()
            SyscallId.SNPRINTF  -> stdio.snprintf()

            // stdlib
            SyscallId.MALLOC    -> stdlib.malloc()
            SyscallId.FREE      -> stdlib.free()
            SyscallId.EXIT      -> stdlib.exit()
            SyscallId.ATOI      -> stdlib.atoi()
            SyscallId.ABS       -> stdlib.abs()
            SyscallId.RAND      -> stdlib.rand()
            SyscallId.SRAND     -> stdlib.srand()
            SyscallId.CALLOC    -> stdlib.calloc()

            // string
            SyscallId.STRLEN    -> strings.strlen()
            SyscallId.STRCPY    -> strings.strcpy()
            SyscallId.STRNCPY   -> strings.strncpy()
            SyscallId.STRCAT    -> strings.strcat()
            SyscallId.STRCMP    -> strings.strcmp()
            SyscallId.STRNCMP   -> strings.strncmp()
            SyscallId.STRCHR    -> strings.strchr()
            SyscallId.STRSTR    -> strings.strstr()
            SyscallId.MEMCPY    -> strings.memcpy()
            SyscallId.MEMMOVE   -> strings.memmove()
            SyscallId.MEMSET    -> strings.memset()
            SyscallId.MEMCMP    -> strings.memcmp()
            SyscallId.STRDUP    -> strings.strdup()

            // windows
            SyscallId.GET_LAST_ERROR            -> windows.getLastError()
            SyscallId.SET_LAST_ERROR            -> windows.setLastError()
            SyscallId.SLEEP                     -> windows.sleep()
            SyscallId.GET_TICK_COUNT            -> windows.getTickCount()
            SyscallId.EXIT_PROCESS              -> windows.exitProcess()
            SyscallId.GET_SYSTEM_TIME           -> windows.getSystemTime()
            SyscallId.QUERY_PERFORMANCE_COUNTER -> windows.queryPerformanceCounter()
            SyscallId.QUERY_PERFORMANCE_FREQUENCY -> windows.queryPerformanceFrequency()
            SyscallId.OUTPUT_DEBUG_STRING       -> windows.outputDebugString()

            else -> throw VmRuntimeException("Unknown syscall ID: 0x${id.toString(16)}")
        }
    }
}
