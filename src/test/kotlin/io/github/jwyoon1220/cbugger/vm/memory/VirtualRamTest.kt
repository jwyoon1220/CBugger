package io.github.jwyoon1220.cbugger.vm.memory

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class VirtualRamTest {

    @Test
    fun `read and write int roundtrip`() {
        val ram = VirtualRam(1024)
        ram.writeInt(4, 42)
        assertEquals(42, ram.readInt(4))
    }

    @Test
    fun `read and write byte roundtrip`() {
        val ram = VirtualRam(1024)
        ram.writeByte(1, 0x7F)
        assertEquals(0x7F.toByte(), ram.readByte(1))
    }

    @Test
    fun `null pointer dereference on read`() {
        val ram = VirtualRam(1024)
        assertThrows<NullPointerDereferenceException> { ram.readInt(0) }
    }

    @Test
    fun `null pointer dereference on write`() {
        val ram = VirtualRam(1024)
        assertThrows<NullPointerDereferenceException> { ram.writeInt(0, 1) }
    }

    @Test
    fun `segmentation fault on out of bounds read`() {
        val ram = VirtualRam(1024)
        assertThrows<SegmentationFaultException> { ram.readInt(1022) } // 1022+4 > 1024
    }

    @Test
    fun `segmentation fault on negative address`() {
        val ram = VirtualRam(1024)
        assertThrows<SegmentationFaultException> { ram.readInt(-1) }
    }

    @Test
    fun `capacity is correct`() {
        val ram = VirtualRam(2048)
        assertEquals(2048, ram.capacity)
    }
}
