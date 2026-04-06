package io.github.jwyoon1220.cbugger.vm.memory

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HeapAllocatorTest {

    private fun makeAllocator(heapSize: Int = 4096): HeapAllocator {
        val ram = VirtualRam(heapSize * 2 + 64)
        return HeapAllocator(ram, 4, heapSize)
    }

    @Test
    fun `malloc returns non-zero pointer`() {
        val alloc = makeAllocator()
        val ptr = alloc.malloc(16)
        assertNotEquals(0, ptr)
    }

    @Test
    fun `malloc returns 0 on OOM`() {
        val alloc = makeAllocator(heapSize = 16)
        // Only enough for one small block (header=8 + data)
        val ptr1 = alloc.malloc(4)
        assertNotEquals(0, ptr1)
        // Second allocation should fail
        val ptr2 = alloc.malloc(4096)
        assertEquals(0, ptr2)
    }

    @Test
    fun `calloc zero-initializes memory`() {
        val ram = VirtualRam(65536)
        val alloc = HeapAllocator(ram, 4, 32768)
        val ptr = alloc.calloc(4, 4)
        assertNotEquals(0, ptr)
        for (i in 0 until 16) {
            assertEquals(0, ram.readByte(ptr + i).toInt())
        }
    }

    @Test
    fun `free allows reuse of block`() {
        val alloc = makeAllocator(4096)
        val ptr1 = alloc.malloc(32)
        assertNotEquals(0, ptr1)
        val bytesBefore = alloc.allocatedBytes
        alloc.free(ptr1)
        val ptr2 = alloc.malloc(32)
        // Reused block; allocated bytes should not grow beyond the first allocation
        assertEquals(ptr1, ptr2)
        assertEquals(bytesBefore, alloc.allocatedBytes)
    }

    @Test
    fun `free null pointer is no-op`() {
        val alloc = makeAllocator()
        alloc.free(0) // must not throw
    }

    @Test
    fun `malloc size zero returns 0`() {
        val alloc = makeAllocator()
        assertEquals(0, alloc.malloc(0))
    }

    @Test
    fun `multiple allocations return distinct pointers`() {
        val alloc = makeAllocator(4096)
        val ptrs = (1..10).map { alloc.malloc(16) }
        assertEquals(ptrs.size, ptrs.toSet().size)
        ptrs.forEach { assertNotEquals(0, it) }
    }
}
