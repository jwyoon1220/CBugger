package io.github.jwyoon1220.cbugger.ui

import io.github.jwyoon1220.cbugger.parser.DebugInfo
import io.github.jwyoon1220.cbugger.vm.VirtualMachine

class DebuggerViewModel(
    private val vm: VirtualMachine,
    private val debugTable: List<DebugInfo>
) {
    var currentIp: Int = 0
        private set
    var currentCLine: Int = -1
        private set
    var errorMessage: String? = null
        private set

    val stackSnapshot = mutableListOf<Int>()

    fun step() {
        if (!vm.isRunning) return
        try {
            vm.step()
            updateUiState()
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    private fun updateUiState() {
        currentIp = vm.ip
        if (currentIp < debugTable.size) {
            currentCLine = debugTable[currentIp].cLine
        }
        stackSnapshot.clear()
        val start = maxOf(0, vm.stack.sp - 10)
        for (i in start..vm.stack.sp) {
            if (i >= 0) stackSnapshot.add(vm.stack.peek())
        }
    }
}
