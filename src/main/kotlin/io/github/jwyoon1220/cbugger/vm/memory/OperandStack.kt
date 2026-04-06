package io.github.jwyoon1220.cbugger.vm.memory

import it.unimi.dsi.fastutil.ints.IntArrayList

class OperandStack(capacity: Int = 1024) {
    private val stack = IntArrayList(capacity)
    var sp: Int = -1
        private set

    init {
        stack.size(capacity)
    }

    fun push(value: Int) {
        sp++
        if (sp >= stack.size) {
            stack.size(stack.size * 2)
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

    fun isEmpty(): Boolean = sp < 0

    fun size(): Int = sp + 1
}
