package io.github.jwyoon1220.cbugger.ui.components

// TODO: Implement with Compose Multiplatform once UI framework dependency is resolved
// This is a placeholder for the DualViewer Compose component

object DualViewer {
    fun render(currentCLine: Int, currentIp: Int, cSource: List<String>, asmSource: List<String>): String {
        val sb = StringBuilder()
        sb.appendLine("=== C Source ===")
        cSource.forEachIndexed { index, line ->
            val marker = if (index + 1 == currentCLine) ">>>" else "   "
            sb.appendLine("$marker ${index + 1}: $line")
        }
        sb.appendLine()
        sb.appendLine("=== Assembly ===")
        asmSource.forEachIndexed { index, line ->
            val marker = if (index == currentIp) ">>>" else "   "
            sb.appendLine("$marker $index: $line")
        }
        return sb.toString()
    }
}
