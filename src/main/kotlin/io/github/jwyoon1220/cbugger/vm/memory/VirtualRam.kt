package io.github.jwyoon1220.cbugger.vm.memory

import org.agrona.concurrent.UnsafeBuffer
import java.nio.ByteBuffer

class VirtualRam(capacityBytes: Int) {
    private val buffer = UnsafeBuffer(ByteBuffer.allocateDirect(capacityBytes))

    fun readInt(address: Int): Int {
        if (address == 0) throw NullPointerDereferenceException()
        checkBounds(address, 4)
        return buffer.getInt(address)
    }

    fun writeInt(address: Int, value: Int) {
        if (address == 0) throw NullPointerDereferenceException()
        checkBounds(address, 4)
        buffer.putInt(address, value)
    }

    fun readByte(address: Int): Byte {
        if (address == 0) throw NullPointerDereferenceException()
        checkBounds(address, 1)
        return buffer.getByte(address)
    }

    fun writeByte(address: Int, value: Byte) {
        if (address == 0) throw NullPointerDereferenceException()
        checkBounds(address, 1)
        buffer.putByte(address, value)
    }

    val capacity: Int get() = buffer.capacity()

    /** Reads a null-terminated string from [address]. Returns empty string if address is 0. */
    fun readString(address: Int): String {
        if (address == 0) return ""
        val sb = StringBuilder()
        var ptr = address
        while (true) {
            checkBounds(ptr, 1)
            val b = buffer.getByte(ptr)
            if (b == 0.toByte()) break
            sb.append(b.toInt().toChar())
            ptr++
            if (ptr - address > 65535) break // safety limit
        }
        return sb.toString()
    }

    /** Writes a null-terminated string to [address]. Returns the address of the null terminator. */
    fun writeString(address: Int, value: String): Int {
        if (address == 0) throw NullPointerDereferenceException()
        var ptr = address
        for (ch in value) {
            checkBounds(ptr, 1)
            buffer.putByte(ptr++, ch.code.toByte())
        }
        checkBounds(ptr, 1)
        buffer.putByte(ptr, 0)
        return ptr
    }

    /** Copies [count] bytes from [src] to [dst]. Handles overlapping regions. */
    fun copyBytes(dst: Int, src: Int, count: Int) {
        if (count <= 0) return
        checkBounds(dst, count)
        checkBounds(src, count)
        if (dst <= src || dst >= src + count) {
            for (i in 0 until count) buffer.putByte(dst + i, buffer.getByte(src + i))
        } else {
            // Overlap: backward copy (memmove semantics)
            for (i in count - 1 downTo 0) buffer.putByte(dst + i, buffer.getByte(src + i))
        }
    }

    /** Sets [count] bytes at [address] to [value]. */
    fun fillBytes(address: Int, value: Byte, count: Int) {
        if (count <= 0) return
        checkBounds(address, count)
        for (i in 0 until count) buffer.putByte(address + i, value)
    }

    /** Reads [count] raw bytes from [address] into a ByteArray. */
    fun readBytes(address: Int, count: Int): ByteArray {
        if (count <= 0) return ByteArray(0)
        checkBounds(address, count)
        return ByteArray(count) { buffer.getByte(address + it) }
    }

    private fun checkBounds(address: Int, size: Int) {
        if (address < 0 || address + size > buffer.capacity()) {
            throw SegmentationFaultException(address)
        }
    }
}
