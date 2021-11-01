import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTests {
    @Test
    fun difficultCase() {
        val expected = """
            Subtraction
            |   Subtraction
            |   |   Multiplication
            |   |   |   Num(value=2.0, position=2)
            |   |   |   Sum
            |   |   |   |   Multiplication
            |   |   |   |   |   Num(value=5.0, position=6)
            |   |   |   |   |   Num(value=2.0, position=9)
            |   |   |   |   Multiplication
            |   |   |   |   |   Num(value=6.0, position=11)
            |   |   |   |   |   Num(value=3.0, position=14)
            |   |   UnaryMinus(Num(value=4.0, position=19))
            |   UnaryPlus(Num(value=4.0, position=25))
            """.trimIndent()
        assertEquals(expected, tokenizeAndParse(TestUtils.bigString).toString())
    }

    @Test
    fun failure() {
        val fail1 = runCatching { tokenizeAndParse("  2 +") }.exceptionOrNull()
        val fail2 = runCatching { tokenizeAndParse("  2 + 3 * ()") }.exceptionOrNull()
        val fail3 = runCatching { tokenizeAndParse("") }.exceptionOrNull()
        val fail4 = runCatching { tokenizeAndParse("  2 + 3 * (2") }.exceptionOrNull()
        val fail5 = runCatching { tokenizeAndParse("  2 + 3 * (2 3") }.exceptionOrNull()
        val fail6 = runCatching { tokenizeAndParse("2 2") }.exceptionOrNull()
        assertTrue(fail1 is IllegalArgumentException)
        assertTrue(fail2 is IllegalArgumentException)
        assertTrue(fail3 is IllegalArgumentException)
        assertTrue(fail4 is IllegalArgumentException)
        assertTrue(fail5 is IllegalArgumentException)
        assertTrue(fail6 is IllegalArgumentException)
        assertEquals("Expected an argument for '+' at position 6 but found EOS", fail1.message)
        assertEquals("Expected an argument for '(' at position 12 but found ')'", fail2.message)
        assertEquals("Empty input", fail3.message)
        assertEquals("Expected ')' at position 13 for '(' at position 11 but found EOS", fail4.message)
        assertEquals("Expected ')' at position 14 for '(' at position 11 but found 3", fail5.message)
        assertEquals("Expected EOS at position 3 but found '2'", fail6.message)
    }
}