package io.github.jwyoon1220.cbugger.parser

import io.github.jwyoon1220.cbugger.vm.isa.OpCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FastAsmParserTest {

    @Test
    fun `parse single HALT instruction`() {
        val parser = FastAsmParser()
        parser.parse("HALT")
        val bc = parser.toBytecodeArray()
        assertEquals(1, bc.size)
        assertEquals(OpCode.HALT, bc[0])
    }

    @Test
    fun `parse PI with operand`() {
        val parser = FastAsmParser()
        parser.parse("PI 42")
        val bc = parser.toBytecodeArray()
        val ops = parser.toOperandsArray()
        assertEquals(OpCode.PI, bc[0])
        assertEquals(42, ops[0])
    }

    @Test
    fun `parse negative operand`() {
        val parser = FastAsmParser()
        parser.parse("PI -5")
        assertEquals(-5, parser.toOperandsArray()[0])
    }

    @Test
    fun `parse hex operand`() {
        val parser = FastAsmParser()
        parser.parse("PI 0xFF")
        assertEquals(255, parser.toOperandsArray()[0])
    }

    @Test
    fun `parse multiple instructions`() {
        val parser = FastAsmParser()
        parser.parse("""
            PI 10
            PI 32
            ADD
            HALT
        """.trimIndent())
        val bc = parser.toBytecodeArray()
        assertEquals(4, bc.size)
        assertEquals(OpCode.PI, bc[0])
        assertEquals(OpCode.PI, bc[1])
        assertEquals(OpCode.ADD, bc[2])
        assertEquals(OpCode.HALT, bc[3])
    }

    @Test
    fun `parse skips line comments`() {
        val parser = FastAsmParser()
        parser.parse("""
            // This is a comment
            PI 1
            ; another comment
            PI 2
        """.trimIndent())
        assertEquals(2, parser.toBytecodeArray().size)
    }

    @Test
    fun `parse line directive sets debug info`() {
        val parser = FastAsmParser()
        parser.parse("""
            .line 5
            PI 1
        """.trimIndent())
        assertEquals(1, parser.debugTable.size)
        assertEquals(5, parser.debugTable[0].cLine)
    }

    @Test
    fun `parse skips unknown mnemonics`() {
        val parser = FastAsmParser()
        parser.parse("""
            UNKNOWN_OP 99
            PI 1
        """.trimIndent())
        assertEquals(1, parser.toBytecodeArray().size)
        assertEquals(OpCode.PI, parser.toBytecodeArray()[0])
    }

    @Test
    fun `parse empty input produces empty output`() {
        val parser = FastAsmParser()
        parser.parse("")
        assertTrue(parser.toBytecodeArray().isEmpty())
        assertTrue(parser.toOperandsArray().isEmpty())
        assertTrue(parser.debugTable.isEmpty())
    }

    @Test
    fun `parse resets state on second call`() {
        val parser = FastAsmParser()
        parser.parse("PI 1\nPI 2")
        assertEquals(2, parser.toBytecodeArray().size)
        parser.parse("HALT")
        assertEquals(1, parser.toBytecodeArray().size)
    }
}
