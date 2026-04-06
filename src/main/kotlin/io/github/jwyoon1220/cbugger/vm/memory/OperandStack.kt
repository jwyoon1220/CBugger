package io.github.jwyoon1220.cbugger.vm.memory

import it.unimi.dsi.fastutil.ints.IntArrayList

class OperandStack(capacity: Int = 1024) {
    private val stack = IntArrayList(capacity)
    var sp: Int = -1
        private set

    init {
        stack.size(capacity)
    }

    companion object {
        private const val LARGE_STACK_THRESHOLD = 65536
    }

    fun push(value: Int) {
        sp++
        if (sp >= stack.size) {
            val newSize = if (stack.size < LARGE_STACK_THRESHOLD) stack.size * 2 else stack.size + stack.size / 2
            stack.size(newSize)
        }
        stack.set(sp, value)
    }

    fun pop(): Int {
        if (sp < 0) throw StackUnderflowException()
        return stack.getInt(sp--)
    }

    fun peek(): Int {
        if (sp < 0) throw StackUnderflowException()
        return stack.getInt(sp)
    }

    fun getAt(index: Int): Int {
        if (index < 0 || index > sp) throw IndexOutOfBoundsException("Index $index out of stack bounds [0, $sp]")
        return stack.getInt(index)
    }

    fun isEmpty(): Boolean = sp < 0

    fun size(): Int = sp + 1
}
