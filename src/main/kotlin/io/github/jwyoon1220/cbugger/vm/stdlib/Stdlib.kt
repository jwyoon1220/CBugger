package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.VmRuntimeException
import io.github.jwyoon1220.cbugger.vm.memory.HeapAllocator
import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import kotlin.math.abs as kAbs
import kotlin.random.Random

/**
 * VM implementation of stdlib.h functions.
 */
internal class Stdlib(
    private val ram: VirtualRam,
    private val stack: OperandStack,
    private val heap: HeapAllocator
) {
    private var rng = Random(0)

    /** malloc(size) — allocates size bytes, pushes ptr (0 on OOM) */
    fun malloc() {
        val size = stack.pop()
        stack.push(heap.malloc(size))
    }

    /** calloc(nmemb, size) — allocates nmemb*size zero-initialized bytes */
    fun calloc() {
        val size  = stack.pop()
        val nmemb = stack.pop()
        stack.push(heap.calloc(nmemb, size))
    }

    /** free(ptr) — marks the block at ptr as free */
    fun free() {
        val ptr = stack.pop()
        heap.free(ptr)
    }

    /** exit(code) — halts the VM by throwing a controlled exception */
    fun exit() {
        val code = stack.pop()
        throw VmExitException(code)
    }

    /** atoi(s) — parses the integer prefix of the string at ptr */
    fun atoi() {
        val ptr = stack.pop()
        val s = ram.readString(ptr).trimStart()
        var i = 0
        var negative = false
        if (i < s.length && s[i] == '-') { negative = true; i++ }
        else if (i < s.length && s[i] == '+') i++
        var result = 0
        while (i < s.length && s[i].isDigit()) {
            result = result * 10 + (s[i] - '0')
            i++
        }
        stack.push(if (negative) -result else result)
    }

    /** abs(n) — absolute value */
    fun abs() {
        val n = stack.pop()
        stack.push(kAbs(n))
    }

    /** rand() — pushes a non-negative pseudo-random integer */
    fun rand() {
        stack.push(rng.nextInt() and Int.MAX_VALUE)
    }

    /** srand(seed) — seeds the random number generator */
    fun srand() {
        val seed = stack.pop()
        rng = Random(seed.toLong() and 0xFFFFFFFFL)
    }
}

/** Thrown by `exit()` and `ExitProcess()` to halt the VM cleanly. */
class VmExitException(val code: Int) : RuntimeException("Process exited with code $code")
