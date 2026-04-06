package io.github.jwyoon1220.cbugger.parser

data class DebugInfo(
    val ip: Int,
    val cLine: Int,
    val asmLine: Int
)
