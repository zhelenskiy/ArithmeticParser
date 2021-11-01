import kotlin.test.*

class TokenizerTests {
    @Test
    fun difficultCase() {
        val tokens = tokenize(TestUtils.bigString)
        assertContentEquals(listOf(
            Num(2.0, 2), Asterisk(3), LPar(4), Num(5.0, 6), Asterisk(7), Num(2.0, 9), Plus(10), Num(6.0, 11),
            Asterisk(12), Num(3.0, 14), RPar(15), Minus(16), Minus(17), Num(4.0, 19),
            Minus(20), LPar(21), Plus(22), LPar(24), Num(4.0, 25), RPar(26), RPar(28)
        ), tokens)
    }

    @Test
    fun failure() {
        val actual = runCatching { tokenize("1  q") }.exceptionOrNull()
        assertTrue(actual is IllegalArgumentException)
        assertEquals("Unexpected character 'q' at position 4", actual.message)
    }
}