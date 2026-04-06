package io.github.jwyoon1220.cbugger.parser

object FastArgParser {
    fun parseArg(source: String, start: Int, end: Int): Int {
        var cursor = start
        var negative = false
        var result = 0

        if (cursor < end && source[cursor] == '-') {
            negative = true
            cursor++
        }

        if (cursor < end && source[cursor] == '0' && cursor + 1 < end) {
            val next = source[cursor + 1]
            if (next == 'x' || next == 'X') {
                // Hexadecimal
                cursor += 2
                while (cursor < end) {
                    val ch = source[cursor]
                    result = result * 16 + hexDigit(ch)
                    cursor++
                }
                return if (negative) -result else result
            }
        }

        while (cursor < end) {
            val ch = source[cursor]
            if (ch < '0' || ch > '9') break
            result = result * 10 + (ch - '0')
            cursor++
        }

        return if (negative) -result else result
    }

    private fun hexDigit(ch: Char): Int = when (ch) {
        in '0'..'9' -> ch - '0'
        in 'a'..'f' -> ch - 'a' + 10
        in 'A'..'F' -> ch - 'A' + 10
        else -> throw IllegalArgumentException("Invalid hex digit: $ch")
    }
}
