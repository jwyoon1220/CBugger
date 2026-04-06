package io.github.jwyoon1220.cbugger.vm

import io.github.jwyoon1220.cbugger.vm.isa.OpCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VirtualMachineTest {

    private fun makeVm(vararg instructions: Pair<Byte, Int>): VirtualMachine {
        val bytecode = ByteArray(instructions.size) { instructions[it].first }
        val operands = IntArray(instructions.size) { instructions[it].second }
        return VirtualMachine(bytecode, operands)
    }

    @Test
    fun `HALT stops execution`() {
        val vm = makeVm(OpCode.HALT to 0)
        vm.run()
        assertFalse(vm.isRunning)
    }

    @Test
    fun `PI pushes integer onto stack`() {
        val vm = makeVm(OpCode.PI to 42, OpCode.HALT to 0)
        vm.step() // PI
        assertEquals(42, vm.stack.peek())
    }

    @Test
    fun `ADD pops two values and pushes sum`() {
        val vm = makeVm(
            OpCode.PI to 10,
            OpCode.PI to 32,
            OpCode.ADD to 0,
            OpCode.HALT to 0
        )
        vm.run()
        // After HALT, last result stays on stack (HALT doesn't pop)
        // Run up to ADD
        val vm2 = makeVm(OpCode.PI to 10, OpCode.PI to 32, OpCode.ADD to 0)
        vm2.run()
        assertEquals(42, vm2.stack.peek())
    }

    @Test
    fun `SUB computes left minus right`() {
        val vm = makeVm(OpCode.PI to 100, OpCode.PI to 37, OpCode.SUB to 0)
        vm.run()
        assertEquals(63, vm.stack.peek())
    }

    @Test
    fun `MUL multiplies two values`() {
        val vm = makeVm(OpCode.PI to 6, OpCode.PI to 7, OpCode.MUL to 0)
        vm.run()
        assertEquals(42, vm.stack.peek())
    }

    @Test
    fun `DIV divides left by right`() {
        val vm = makeVm(OpCode.PI to 84, OpCode.PI to 2, OpCode.DIV to 0)
        vm.run()
        assertEquals(42, vm.stack.peek())
    }

    @Test
    fun `DIV by zero throws VmRuntimeException`() {
        val vm = makeVm(OpCode.PI to 5, OpCode.PI to 0, OpCode.DIV to 0)
        assertThrows<VmRuntimeException> { vm.run() }
        assertFalse(vm.isRunning)
    }

    @Test
    fun `CMP_EQ pushes 1 when equal`() {
        val vm = makeVm(OpCode.PI to 5, OpCode.PI to 5, OpCode.CMP_EQ to 0)
        vm.run()
        assertEquals(1, vm.stack.peek())
    }

    @Test
    fun `CMP_EQ pushes 0 when not equal`() {
        val vm = makeVm(OpCode.PI to 5, OpCode.PI to 6, OpCode.CMP_EQ to 0)
        vm.run()
        assertEquals(0, vm.stack.peek())
    }

    @Test
    fun `CMP_LT pushes 1 when left less than right`() {
        val vm = makeVm(OpCode.PI to 3, OpCode.PI to 5, OpCode.CMP_LT to 0)
        vm.run()
        assertEquals(1, vm.stack.peek())
    }

    @Test
    fun `JMP jumps to target ip`() {
        // IP 0: JMP -> 2, IP 1: PI 99 (should be skipped), IP 2: HALT
        val vm = makeVm(OpCode.JMP to 2, OpCode.PI to 99, OpCode.HALT to 0)
        vm.run()
        assertTrue(vm.stack.isEmpty())
    }

    @Test
    fun `JMP_Z jumps when condition is zero`() {
        // PI 0, JMP_Z -> 3, PI 99 (skipped), HALT
        val vm = makeVm(
            OpCode.PI to 0,
            OpCode.JMP_Z to 3,
            OpCode.PI to 99,
            OpCode.HALT to 0
        )
        vm.run()
        assertTrue(vm.stack.isEmpty())
    }

    @Test
    fun `JMP_NZ does not jump when condition is zero`() {
        // PI 0, JMP_NZ -> 3, PI 42, HALT
        val vm = makeVm(
            OpCode.PI to 0,
            OpCode.JMP_NZ to 3,
            OpCode.PI to 42,
            OpCode.HALT to 0
        )
        vm.run()
        assertEquals(42, vm.stack.peek())
    }

    @Test
    fun `PT_STORE and PT_LOAD roundtrip`() {
        // PI addr=4, PI value=777, PT_STORE, PI addr=4, PT_LOAD
        val vm = makeVm(
            OpCode.PI to 4,
            OpCode.PI to 777,
            OpCode.PT_STORE to 0,
            OpCode.PI to 4,
            OpCode.PT_LOAD to 0
        )
        vm.run()
        assertEquals(777, vm.stack.peek())
    }

    @Test
    fun `reset resets ip and isRunning`() {
        val vm = makeVm(OpCode.HALT to 0)
        vm.run()
        assertFalse(vm.isRunning)
        vm.reset()
        assertTrue(vm.isRunning)
        assertEquals(0, vm.ip)
    }
}
