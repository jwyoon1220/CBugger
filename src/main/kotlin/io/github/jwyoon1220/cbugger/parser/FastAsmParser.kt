package io.github.jwyoon1220.cbugger.parser

import io.github.jwyoon1220.cbugger.vm.isa.OpCode
import it.unimi.dsi.fastutil.bytes.ByteArrayList
import it.unimi.dsi.fastutil.ints.IntArrayList

class FastAsmParser {
    val bytecode = ByteArrayList()
    val operands = IntArrayList()
    val debugTable = mutableListOf<DebugInfo>()

    fun parse(source: String) {
        bytecode.clear()
        operands.clear()
        debugTable.clear()

        var cursor = 0
        var currentCLine = -1
        var ip = 0

        while (cursor < source.length) {
            // Skip whitespace
            while (cursor < source.length && source[cursor].isWhitespace()) cursor++
            if (cursor >= source.length) break

            // Skip // comments
            if (cursor + 1 < source.length && source[cursor] == '/' && source[cursor + 1] == '/') {
                while (cursor < source.length && source[cursor] != '\n') cursor++
                continue
            }
            // Skip ; comments
            if (source[cursor] == ';') {
                while (cursor < source.length && source[cursor] != '\n') cursor++
                continue
            }

            // Process directives
            if (source[cursor] == '.') {
                cursor++
                val start = cursor
                while (cursor < source.length && source[cursor].isLetter()) cursor++
                val directive = source.substring(start, cursor)
                if (directive == "line") {
                    while (cursor < source.length && source[cursor] == ' ') cursor++
                    val numStart = cursor
                    while (cursor < source.length && source[cursor].isDigit()) cursor++
                    currentCLine = FastArgParser.parseArg(source, numStart, cursor)
                }
                while (cursor < source.length && source[cursor] != '\n') cursor++
                continue
            }

            // Read instruction mnemonic
            val mnemonicStart = cursor
            while (cursor < source.length && !source[cursor].isWhitespace()) cursor++
            val mnemonic = source.substring(mnemonicStart, cursor).uppercase()

            if (mnemonic.isEmpty()) continue

            // Skip spaces (but not newline)
            while (cursor < source.length && source[cursor] == ' ') cursor++

            // Map mnemonic to opcode
            val opcode: Byte? = when (mnemonic) {
                "HALT" -> OpCode.HALT
                "PI" -> OpCode.PI
                "PPI" -> OpCode.PPI
                "PT_LOAD" -> OpCode.PT_LOAD
                "PT_STORE" -> OpCode.PT_STORE
                "PT_ADD" -> OpCode.PT_ADD
                "ADD" -> OpCode.ADD
                "SUB" -> OpCode.SUB
                "MUL" -> OpCode.MUL
                "DIV" -> OpCode.DIV
                "MOD" -> OpCode.MOD
                "CMP_EQ" -> OpCode.CMP_EQ
                "CMP_NE" -> OpCode.CMP_NE
                "CMP_LT" -> OpCode.CMP_LT
                "CMP_GT" -> OpCode.CMP_GT
                "JMP" -> OpCode.JMP
                "JMP_Z" -> OpCode.JMP_Z
                "JMP_NZ" -> OpCode.JMP_NZ
                "CALL" -> OpCode.CALL
                "RET" -> OpCode.RET
                "LOAD_LOCAL" -> OpCode.LOAD_LOCAL
                "STORE_LOCAL" -> OpCode.STORE_LOCAL
                "SYSCALL" -> OpCode.SYSCALL
                else -> null
            }

            if (opcode == null) {
                // Skip unknown mnemonic line
                while (cursor < source.length && source[cursor] != '\n') cursor++
                continue
            }

            // Parse optional operand
            val operandValue: Int
            if (cursor < source.length && source[cursor] != '\n' && source[cursor] != ';' &&
                !(cursor + 1 < source.length && source[cursor] == '/' && source[cursor + 1] == '/')) {
                val argStart = cursor
                while (cursor < source.length && source[cursor] != '\n' && source[cursor] != ';' &&
                    !(cursor + 1 < source.length && source[cursor] == '/' && source[cursor + 1] == '/')) {
                    cursor++
                }
                val argEnd = cursor
                var trimEnd = argEnd
                while (trimEnd > argStart && source[trimEnd - 1] == ' ') trimEnd--
                operandValue = if (trimEnd > argStart) FastArgParser.parseArg(source, argStart, trimEnd) else 0
            } else {
                operandValue = 0
            }

            bytecode.add(opcode)
            operands.add(operandValue)
            debugTable.add(DebugInfo(ip, currentCLine, ip))
            ip++
        }
    }

    fun toBytecodeArray(): ByteArray = bytecode.toByteArray()
    fun toOperandsArray(): IntArray = operands.toIntArray()
}
