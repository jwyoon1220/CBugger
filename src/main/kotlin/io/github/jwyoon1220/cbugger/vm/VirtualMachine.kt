package io.github.jwyoon1220.cbugger.vm

import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import io.github.jwyoon1220.cbugger.vm.isa.OpCode

class VirtualMachine(
    private val bytecode: ByteArray,
    private val operands: IntArray
) {
    val ram = VirtualRam(1024 * 1024) // 1MB Off-heap
    val stack = OperandStack()

    var ip: Int = 0
        private set
    var isRunning: Boolean = true
        private set

    fun step() {
        if (ip >= bytecode.size || !isRunning) return

        val opcode = bytecode[ip]

        try {
            when (opcode) {
                OpCode.HALT -> isRunning = false

                OpCode.PI -> {
                    stack.push(operands[ip])
                    ip++
                }

                OpCode.PPI -> {
                    stack.pop()
                    ip++
                }

                OpCode.PT_LOAD -> {
                    val ptr = stack.pop()
                    val value = ram.readInt(ptr)
                    stack.push(value)
                    ip++
                }

                OpCode.PT_STORE -> {
                    val value = stack.pop()
                    val ptr = stack.pop()
                    ram.writeInt(ptr, value)
                    ip++
                }

                OpCode.PT_ADD -> {
                    val offset = stack.pop()
                    val ptr = stack.pop()
                    stack.push(ptr + offset * 4)
                    ip++
                }

                OpCode.ADD -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(left + right)
                    ip++
                }

                OpCode.SUB -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(left - right)
                    ip++
                }

                OpCode.MUL -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(left * right)
                    ip++
                }

                OpCode.DIV -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    if (right == 0) throw VmRuntimeException("Division by zero at IP: $ip")
                    stack.push(left / right)
                    ip++
                }

                OpCode.MOD -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    if (right == 0) throw VmRuntimeException("Modulo by zero at IP: $ip")
                    stack.push(left % right)
                    ip++
                }

                OpCode.CMP_EQ -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(if (left == right) 1 else 0)
                    ip++
                }

                OpCode.CMP_NE -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(if (left != right) 1 else 0)
                    ip++
                }

                OpCode.CMP_LT -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(if (left < right) 1 else 0)
                    ip++
                }

                OpCode.CMP_GT -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(if (left > right) 1 else 0)
                    ip++
                }

                OpCode.JMP -> {
                    ip = operands[ip]
                }

                OpCode.JMP_Z -> {
                    val cond = stack.pop()
                    ip = if (cond == 0) operands[ip] else ip + 1
                }

                OpCode.JMP_NZ -> {
                    val cond = stack.pop()
                    ip = if (cond != 0) operands[ip] else ip + 1
                }

                else -> {
                    throw VmRuntimeException("Unknown opcode: 0x${opcode.toString(16)} at IP: $ip")
                }
            }
        } catch (e: VmRuntimeException) {
            isRunning = false
            throw e
        } catch (e: Exception) {
            isRunning = false
            throw VmRuntimeException("Runtime Error at IP: $ip", e)
        }
    }

    fun run() {
        while (isRunning && ip < bytecode.size) {
            step()
        }
    }

    fun reset() {
        ip = 0
        isRunning = true
    }
}
