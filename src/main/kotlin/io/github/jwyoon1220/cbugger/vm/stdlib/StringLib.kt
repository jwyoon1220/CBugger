package io.github.jwyoon1220.cbugger.vm.stdlib

import io.github.jwyoon1220.cbugger.vm.memory.HeapAllocator
import io.github.jwyoon1220.cbugger.vm.memory.OperandStack
import io.github.jwyoon1220.cbugger.vm.memory.VirtualRam

/**
 * VM implementation of string.h functions.
 */
internal class StringLib(
    private val ram: VirtualRam,
    private val stack: OperandStack,
    private val heap: HeapAllocator
) {
    /** strlen(s) — number of chars before null terminator */
    fun strlen() {
        val ptr = stack.pop()
        val s = ram.readString(ptr)
        stack.push(s.length)
    }

    /** strcpy(dst, src) — copies src to dst, pushes dst */
    fun strcpy() {
        val src = stack.pop()
        val dst = stack.pop()
        val s = ram.readString(src)
        ram.writeString(dst, s)
        stack.push(dst)
    }

    /** strncpy(dst, src, n) — copies at most n chars; pads with nulls */
    fun strncpy() {
        val n   = stack.pop()
        val src = stack.pop()
        val dst = stack.pop()
        if (n <= 0) { stack.push(dst); return }
        val s = ram.readString(src)
        val len = minOf(s.length, n)
        for (i in 0 until len) ram.writeByte(dst + i, s[i].code.toByte())
        for (i in len until n)  ram.writeByte(dst + i, 0)
        stack.push(dst)
    }

    /** strcat(dst, src) — appends src to dst, pushes dst */
    fun strcat() {
        val src = stack.pop()
        val dst = stack.pop()
        val existing = ram.readString(dst)
        val appended = ram.readString(src)
        ram.writeString(dst + existing.length, appended)
        stack.push(dst)
    }

    /** strcmp(s1, s2) — lexicographic compare; returns <0, 0, or >0 */
    fun strcmp() {
        val s2 = stack.pop()
        val s1 = stack.pop()
        val str1 = ram.readString(s1)
        val str2 = ram.readString(s2)
        val result = when {
            str1 < str2 -> -1
            str1 > str2 -> 1
            else        -> 0
        }
        stack.push(result)
    }

    /** strncmp(s1, s2, n) — compare at most n chars */
    fun strncmp() {
        val n  = stack.pop()
        val s2 = stack.pop()
        val s1 = stack.pop()
        val str1 = ram.readString(s1).take(n)
        val str2 = ram.readString(s2).take(n)
        val result = when {
            str1 < str2 -> -1
            str1 > str2 -> 1
            else        -> 0
        }
        stack.push(result)
    }

    /** strchr(s, c) — first occurrence of char c in s; 0 if not found */
    fun strchr() {
        val c   = stack.pop() and 0xFF
        val ptr = stack.pop()
        val s = ram.readString(ptr)
        val idx = s.indexOfFirst { it.code == c }
        stack.push(if (idx < 0) 0 else ptr + idx)
    }

    /** strstr(haystack, needle) — first occurrence of needle in haystack; 0 if not found */
    fun strstr() {
        val needlePtr    = stack.pop()
        val haystackPtr  = stack.pop()
        val haystack = ram.readString(haystackPtr)
        val needle   = ram.readString(needlePtr)
        val idx = haystack.indexOf(needle)
        stack.push(if (idx < 0) 0 else haystackPtr + idx)
    }

    /** memcpy(dst, src, n) — copies n bytes from src to dst (no overlap assumed) */
    fun memcpy() {
        val n   = stack.pop()
        val src = stack.pop()
        val dst = stack.pop()
        ram.copyBytes(dst, src, n)
        stack.push(dst)
    }

    /** memmove(dst, src, n) — copies n bytes, handles overlap */
    fun memmove() {
        val n   = stack.pop()
        val src = stack.pop()
        val dst = stack.pop()
        ram.copyBytes(dst, src, n) // copyBytes already handles overlap
        stack.push(dst)
    }

    /** memset(ptr, c, n) — fills n bytes with c */
    fun memset() {
        val n   = stack.pop()
        val c   = stack.pop() and 0xFF
        val ptr = stack.pop()
        ram.fillBytes(ptr, c.toByte(), n)
        stack.push(ptr)
    }

    /** memcmp(s1, s2, n) — compares n bytes */
    fun memcmp() {
        val n  = stack.pop()
        val s2 = stack.pop()
        val s1 = stack.pop()
        for (i in 0 until n) {
            val b1 = ram.readByte(s1 + i).toInt() and 0xFF
            val b2 = ram.readByte(s2 + i).toInt() and 0xFF
            if (b1 != b2) { stack.push(b1 - b2); return }
        }
        stack.push(0)
    }

    /** strdup(s) — allocates a copy of the string, pushes new ptr (0 on OOM) */
    fun strdup() {
        val ptr = stack.pop()
        val s = ram.readString(ptr)
        val newPtr = heap.malloc(s.length + 1)
        if (newPtr != 0) ram.writeString(newPtr, s)
        stack.push(newPtr)
    }
}
