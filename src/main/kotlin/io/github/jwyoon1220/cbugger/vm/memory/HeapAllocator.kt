package io.github.jwyoon1220.cbugger.vm.memory

/**
 * Simple heap allocator operating within [VirtualRam].
 *
 * Uses a bump pointer for allocation and a free-list for recycling.
 * Each block is prefixed with an 8-byte header: [size: Int][flags: Int]
 * where flags bit 0 = 1 means "free".
 *
 * @param ram        the VM RAM to allocate from
 * @param heapStart  first address available for allocation (must be >= 4)
 * @param heapSize   total bytes available for allocation
 */
class HeapAllocator(
    private val ram: VirtualRam,
    private val heapStart: Int,
    private val heapSize: Int
) {
    companion object {
        private const val HEADER_SIZE = 8   // [size:4][flags:4]
        private const val FLAG_FREE   = 1
    }

    private var bump: Int = heapStart
    private val heapEnd: Int = heapStart + heapSize

    /**
     * Allocates [size] bytes. Returns address of the usable region (after header),
     * or 0 on OOM.
     */
    fun malloc(size: Int): Int {
        if (size <= 0) return 0

        // First-fit search through existing blocks
        var ptr = heapStart
        while (ptr + HEADER_SIZE <= bump) {
            val blockSize = ram.readInt(ptr)
            val flags     = ram.readInt(ptr + 4)
            if (flags and FLAG_FREE != 0 && blockSize >= size) {
                ram.writeInt(ptr + 4, 0) // clear free flag
                return ptr + HEADER_SIZE
            }
            ptr += HEADER_SIZE + blockSize
        }

        // Bump-allocate a new block
        val blockStart = bump
        val needed = HEADER_SIZE + size
        if (blockStart + needed > heapEnd) return 0 // OOM

        ram.writeInt(blockStart, size)
        ram.writeInt(blockStart + 4, 0)
        bump += needed
        return blockStart + HEADER_SIZE
    }

    /**
     * Allocates [nmemb] * [size] bytes, zero-initialized. Returns 0 on OOM.
     */
    fun calloc(nmemb: Int, size: Int): Int {
        val total = nmemb * size
        val ptr = malloc(total)
        if (ptr != 0) ram.fillBytes(ptr, 0, total)
        return ptr
    }

    /**
     * Marks the block at [ptr] as free. No-op for ptr == 0 (C free(NULL) is valid).
     */
    fun free(ptr: Int) {
        if (ptr == 0) return
        val headerAddr = ptr - HEADER_SIZE
        if (headerAddr < heapStart || headerAddr >= heapEnd) return
        ram.writeInt(headerAddr + 4, FLAG_FREE)
    }

    /** Number of bytes currently bump-allocated (including headers). */
    val allocatedBytes: Int get() = bump - heapStart
}
