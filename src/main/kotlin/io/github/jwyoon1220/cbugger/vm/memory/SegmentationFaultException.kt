package io.github.jwyoon1220.cbugger.vm.memory

class SegmentationFaultException(address: Int) : RuntimeException("Segmentation fault: invalid memory access at address 0x${address.toString(16)}")
