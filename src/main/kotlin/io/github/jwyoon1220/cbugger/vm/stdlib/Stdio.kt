package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.memory.HeapAllocator
import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam
import java.io.InputStream
import java.io.PrintStream

/**
 * VM implementation of stdio.h functions.
 *
 * Calling convention (all functions):
 *   - Last argument is on top of stack
 *   - For vararg functions: format string is on top, extra args below
 */
internal class Stdio(
    private val ram: VirtualRam,
    private val stack: OperandStack,
    private val heap: HeapAllocator,
    private val out: PrintStream,
    private val input: InputStream
) {
    private val reader = input.bufferedReader()

    // ── Format string processing ──────────────────────────────────────────────

    /**
     * Formats a C-style format string using [args].
     * Supports: %d, %i, %u, %o, %x, %X, %c, %s, %%, %p, %ld, %li, %lu, %lx, %lX
     */
    private fun format(fmt: String, args: List<Int>): String {
        val sb = StringBuilder()
        var i = 0
        var argIdx = 0
        while (i < fmt.length) {
            if (fmt[i] != '%') {
                sb.append(fmt[i++])
                continue
            }
            i++ // skip '%'
            if (i >= fmt.length) break

            // Flags
            var leftAlign = false
            var zeroPad = false
            if (i < fmt.length && fmt[i] == '-') { leftAlign = true; i++ }
            if (i < fmt.length && fmt[i] == '0') { zeroPad = true; i++ }
            var width = 0
            while (i < fmt.length && fmt[i].isDigit()) { width = width * 10 + (fmt[i] - '0'); i++ }
            // Precision
            var precision = -1
            if (i < fmt.length && fmt[i] == '.') {
                i++
                precision = 0
                while (i < fmt.length && fmt[i].isDigit()) { precision = precision * 10 + (fmt[i] - '0'); i++ }
            }
            // Length modifier
            var longMod = false
            if (i < fmt.length && fmt[i] == 'l') { longMod = true; i++ }
            if (i >= fmt.length) break

            val spec = fmt[i++]
            val arg = if (argIdx < args.size) args[argIdx++] else 0

            val converted: String = when (spec) {
                'd', 'i'      -> arg.toString()
                'u'           -> (arg.toLong() and 0xFFFFFFFFL).toString()
                'o'           -> arg.toUInt().toString(8)
                'x'           -> if (longMod) (arg.toLong() and 0xFFFFFFFFL).toString(16)
                                 else arg.toUInt().toString(16)
                'X'           -> if (longMod) (arg.toLong() and 0xFFFFFFFFL).toString(16).uppercase()
                                 else arg.toUInt().toString(16).uppercase()
                'c'           -> (arg and 0xFF).toChar().toString()
                's'           -> {
                    val s = ram.readString(arg)
                    if (precision >= 0 && s.length > precision) s.substring(0, precision) else s
                }
                'p'           -> "0x${arg.toUInt().toString(16)}"
                '%'           -> { argIdx--; "%" }
                else          -> "%$spec"
            }

            val padded = when {
                width > 0 && converted.length < width -> {
                    val pad = width - converted.length
                    if (leftAlign) converted.padEnd(width)
                    else if (zeroPad) converted.padStart(width, '0')
                    else converted.padStart(width)
                }
                else -> converted
            }
            sb.append(padded)
        }
        return sb.toString()
    }

    /**
     * Counts the number of format specifiers in [fmt] (used to know how many args to pop).
     * %% does not consume an arg.
     */
    private fun countSpecifiers(fmt: String): Int {
        var count = 0
        var i = 0
        while (i < fmt.length) {
            if (fmt[i] != '%') { i++; continue }
            i++
            if (i >= fmt.length) break
            // skip flags, width, precision, length modifier
            while (i < fmt.length && fmt[i] in "-+0 #") i++
            while (i < fmt.length && fmt[i].isDigit()) i++
            if (i < fmt.length && fmt[i] == '.') { i++; while (i < fmt.length && fmt[i].isDigit()) i++ }
            if (i < fmt.length && fmt[i] == 'l') i++
            if (i >= fmt.length) break
            if (fmt[i] != '%') count++
            i++
        }
        return count
    }

    /**
     * Pops [n] items from the stack and returns them as a list (first item = deepest = first arg).
     */
    private fun popArgs(n: Int): List<Int> {
        val arr = IntArray(n) { 0 }
        for (i in n - 1 downTo 0) arr[i] = stack.pop()
        return arr.toList()
    }

    // ── printf ────────────────────────────────────────────────────────────────

    /** printf(fmt, ...) — format string on top of stack */
    fun printf() {
        val fmtPtr = stack.pop()
        val fmt = ram.readString(fmtPtr)
        val argCount = countSpecifiers(fmt)
        val args = popArgs(argCount)
        val result = format(fmt, args)
        out.print(result)
        stack.push(result.length)
    }

    /** putchar(c) — pushes the written character value */
    fun putchar() {
        val c = stack.pop() and 0xFF
        out.print(c.toChar())
        stack.push(c)
    }

    /** puts(s) — writes string + '\n', pushes 0 on success */
    fun puts() {
        val ptr = stack.pop()
        val s = ram.readString(ptr)
        out.println(s)
        stack.push(0)
    }

    /** getchar() — reads one character, pushes its int value (EOF = -1) */
    fun getchar() {
        val ch = reader.read()
        stack.push(ch)
    }

    // ── sprintf ───────────────────────────────────────────────────────────────

    /** sprintf(buf, fmt, ...) — writes formatted string to buf in RAM */
    fun sprintf() {
        val fmtPtr = stack.pop()
        val fmt = ram.readString(fmtPtr)
        val argCount = countSpecifiers(fmt)
        val args = popArgs(argCount)
        val bufPtr = stack.pop()  // buf is deepest, popped after varargs
        val result = format(fmt, args)
        ram.writeString(bufPtr, result)
        stack.push(result.length)
    }

    /** snprintf(buf, n, fmt, ...) — writes at most n-1 chars to buf */
    fun snprintf() {
        val fmtPtr = stack.pop()
        val fmt = ram.readString(fmtPtr)
        val argCount = countSpecifiers(fmt)
        val args = popArgs(argCount)
        val n      = stack.pop()
        val bufPtr = stack.pop()  // buf is deepest, popped after n
        val result = format(fmt, args)
        val truncated = if (result.length >= n) result.substring(0, maxOf(0, n - 1)) else result
        ram.writeString(bufPtr, truncated)
        stack.push(result.length) // returns total that would have been written
    }

    // ── scanf ─────────────────────────────────────────────────────────────────

    /**
     * scanf(fmt, ptr...) — reads from stdin, stores into pointers.
     * Supports %d (int), %s (string). Returns number of items read.
     */
    fun scanf() {
        val fmtPtr = stack.pop()
        val fmt = ram.readString(fmtPtr)
        val argCount = countSpecifiers(fmt)
        val ptrs = popArgs(argCount)

        val line = try { reader.readLine() ?: "" } catch (e: Exception) { "" }
        val tokens = line.trim().split(Regex("\\s+"))
        var read = 0
        var argIdx = 0
        var i = 0
        var tokenIdx = 0

        while (i < fmt.length && argIdx < ptrs.size) {
            if (fmt[i] != '%') { i++; continue }
            i++
            if (i >= fmt.length) break
            if (fmt[i] == 'l') i++ // skip length modifier
            if (i >= fmt.length) break
            val spec = fmt[i++]
            if (spec == '%') continue

            val ptr = ptrs[argIdx++]
            val token = if (tokenIdx < tokens.size) tokens[tokenIdx++] else ""

            when (spec) {
                'd', 'i' -> {
                    val v = token.toIntOrNull() ?: 0
                    ram.writeInt(ptr, v)
                    read++
                }
                's' -> {
                    ram.writeString(ptr, token)
                    read++
                }
                'c' -> {
                    ram.writeByte(ptr, if (token.isNotEmpty()) token[0].code.toByte() else 0)
                    read++
                }
            }
        }
        stack.push(read)
    }
}
