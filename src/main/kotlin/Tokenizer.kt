sealed class Token(position: Int) : TokenOrPar(position)
sealed class TokenOrPar(open val position: Int) {
    override fun toString(): String {
        return javaClass.name.uppercase()
    }
}

data class LPar(override val position: Int) : TokenOrPar(position)
data class RPar(override val position: Int) : TokenOrPar(position)
data class Plus(override val position: Int) : Token(position)
data class Minus(override val position: Int) : Token(position)
data class Asterisk(override val position: Int) : Token(position)
data class Slash(override val position: Int) : Token(position)

data class Num(val value: Double, override val position: Int) : Token(position)

private class Tokenizer(val input: String) {
    var position = 0
        private set

    val charsLeft
        get() = input.length - position

    val curChar: Char?
        get() = if (position in input.indices) input[position] else null

    fun <T : Any> readObject(make: (Char) -> T?): T? = curChar?.let(make)?.also { position++ }
    fun readChar(expected: Char): Char? = readIf { it == expected }
    fun readIf(predicate: (Char) -> Boolean): Char? = readObject { if (predicate(it)) it else null }
}

private tailrec fun Tokenizer.skipSpaces(): Unit =
    if (readIf { it.isWhitespace() } != null) skipSpaces() else Unit

private fun Tokenizer.readNum(): Num? {
    val position = position
    var accumulator = readObject { it.digitToIntOrNull() }?.toDouble() ?: return null
    while (true) {
        val digit = readObject { it.digitToIntOrNull() } ?: break
        accumulator = accumulator * 10 + digit
    }
    return Num(accumulator, position)
}

private fun Tokenizer.readToken(): TokenOrPar? {
    skipSpaces()
    val position = position
    val chain: List<Tokenizer.() -> TokenOrPar?> = listOf(
        { readChar('(')?.let { LPar(position) } },
        { readChar(')')?.let { RPar(position) } },
        { readChar('+')?.let { Plus(position) } },
        { readChar('-')?.let { Minus(position) } },
        { readChar('*')?.let { Asterisk(position) } },
        { readChar('/')?.let { Slash(position) } },
        { readNum() },
    )
    for (tokenChecker in chain) {
        tokenChecker()?.let { return it }
    }
    return null
}

fun tokenize(input: String): List<TokenOrPar> = buildList {
    val tokenizer = Tokenizer(input)
    while (true) {
        val token = tokenizer.readToken()
        when {
            token != null -> add(token)
            tokenizer.charsLeft > 0 -> throw IllegalArgumentException("Unexpected character '${tokenizer.curChar}' at position ${tokenizer.position + 1}")
            else -> break
        }
    }
}