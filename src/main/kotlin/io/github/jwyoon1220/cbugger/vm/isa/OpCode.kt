package io.github.jwyoon1220.cbugger.vm.isa

object OpCode {
    const val HALT: Byte = 0x00

    // Stack Operations
    const val PI: Byte = 0x01   // Push Int
    const val PPI: Byte = 0x02  // Pop Int

    // Pointer Operations
    const val PT_LOAD: Byte = 0x10  // Read *ptr
    const val PT_STORE: Byte = 0x11 // Write *ptr
    const val PT_ADD: Byte = 0x12   // Pointer arithmetic

    // Math & Logic
    const val ADD: Byte = 0x20
    const val SUB: Byte = 0x21
    const val MUL: Byte = 0x22
    const val DIV: Byte = 0x23
    const val MOD: Byte = 0x24

    // Comparison
    const val CMP_EQ: Byte = 0x28
    const val CMP_NE: Byte = 0x29
    const val CMP_LT: Byte = 0x2A
    const val CMP_GT: Byte = 0x2B

    // Control Flow
    const val JMP: Byte = 0x30
    const val JMP_Z: Byte = 0x31
    const val JMP_NZ: Byte = 0x32
    const val CALL: Byte = 0x33
    const val RET: Byte = 0x34

    // Local variable operations
    const val LOAD_LOCAL: Byte = 0x40  // Push local variable
    const val STORE_LOCAL: Byte = 0x41 // Pop into local variable

    // Syscall
    const val SYSCALL: Byte = 0x50  // Call C standard library function
}
