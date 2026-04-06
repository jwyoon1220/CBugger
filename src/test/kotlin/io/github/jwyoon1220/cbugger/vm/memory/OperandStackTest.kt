package io.github.jwyoon1220.cbugger.vm.memory

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OperandStackTest {

    @Test
    fun `push and pop single value`() {
        val stack = OperandStack()
        stack.push(99)
        assertEquals(99, stack.pop())
    }

    @Test
    fun `push multiple values and pop in LIFO order`() {
        val stack = OperandStack()
        stack.push(1)
        stack.push(2)
        stack.push(3)
        assertEquals(3, stack.pop())
        assertEquals(2, stack.pop())
        assertEquals(1, stack.pop())
    }

    @Test
    fun `peek does not remove value`() {
        val stack = OperandStack()
        stack.push(55)
        assertEquals(55, stack.peek())
        assertEquals(55, stack.peek())
        assertEquals(1, stack.size())
    }

    @Test
    fun `isEmpty returns true on empty stack`() {
        val stack = OperandStack()
        assertTrue(stack.isEmpty())
    }

    @Test
    fun `isEmpty returns false after push`() {
        val stack = OperandStack()
        stack.push(0)
        assertFalse(stack.isEmpty())
    }

    @Test
    fun `size tracks correctly`() {
        val stack = OperandStack()
        assertEquals(0, stack.size())
        stack.push(1)
        assertEquals(1, stack.size())
        stack.push(2)
        assertEquals(2, stack.size())
        stack.pop()
        assertEquals(1, stack.size())
    }

    @Test
    fun `pop on empty stack throws StackUnderflowException`() {
        val stack = OperandStack()
        assertThrows<StackUnderflowException> { stack.pop() }
    }

    @Test
    fun `peek on empty stack throws StackUnderflowException`() {
        val stack = OperandStack()
        assertThrows<StackUnderflowException> { stack.peek() }
    }

    @Test
    fun `can push beyond initial capacity`() {
        val stack = OperandStack(4)
        for (i in 0 until 100) stack.push(i)
        for (i in 99 downTo 0) assertEquals(i, stack.pop())
    }
}
