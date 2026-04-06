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

    private fun checkBounds(address: Int, size: Int) {
        if (address < 0 || address + size > buffer.capacity()) {
            throw SegmentationFaultException(address)
        }
    }
}
