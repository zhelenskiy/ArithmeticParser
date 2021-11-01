private class ParserState(val list: List<TokenOrPar>, val input: String) {
    init {
        if (list.isEmpty()) {
            throw IllegalArgumentException("Empty input")
        }
    }

    var curIndex = 0
        private set

    val position
        get() = list.elementAtOrNull(curIndex)?.position ?: input.length

    fun peek(): TokenOrPar? = list.elementAtOrNull(curIndex)
    fun pop(): TokenOrPar? = peek()?.also { curIndex++ }
}

interface ExpressionVisitor<T> {
    fun visit(expression: ArithmeticExpression): T
}
// no need to handle all cases in visitor manually as class is sealed and checking all cases is done by the compiler

sealed class ArithmeticExpression {
    abstract val token: Token
    abstract val arguments: List<ArithmeticExpression>
    fun <T> accept(expressionVisitor: ExpressionVisitor<T>) = expressionVisitor.visit(this)

    override fun toString() = javaClass.name + when (arguments.size) {
        0 -> ""
        1 -> "(${arguments[0]})"
        else -> "\n" + arguments.joinToString(separator = "\n").prependIndent("|   ")
    }
}

sealed class BinaryArithmeticExpression(
    override val token: Token,
    val arg1: ArithmeticExpression,
    val arg2: ArithmeticExpression
) : ArithmeticExpression() {
    override val arguments: List<ArithmeticExpression> = listOf(arg1, arg2)
}

class Sum(plus: Plus, arg1: ArithmeticExpression, arg2: ArithmeticExpression) :
    BinaryArithmeticExpression(plus, arg1, arg2)

class Subtraction(minus: Minus, arg1: ArithmeticExpression, arg2: ArithmeticExpression) :
    BinaryArithmeticExpression(minus, arg1, arg2)

class Multiplication(asterisk: Asterisk, arg1: ArithmeticExpression, arg2: ArithmeticExpression) :
    BinaryArithmeticExpression(asterisk, arg1, arg2)

class Division(slash: Slash, arg1: ArithmeticExpression, arg2: ArithmeticExpression) :
    BinaryArithmeticExpression(slash, arg1, arg2)

sealed class UnaryArithmeticExpression(override val token: Token, val arg: ArithmeticExpression) :
    ArithmeticExpression() {
    override val arguments: List<ArithmeticExpression> = listOf(arg)
}

class UnaryMinus(minus: Minus, arg: ArithmeticExpression) : UnaryArithmeticExpression(minus, arg)
class UnaryPlus(plus: Plus, arg: ArithmeticExpression) : UnaryArithmeticExpression(plus, arg)
class ArithmeticExpressionFromNum(override val token: Num) : ArithmeticExpression() {
    override val arguments: List<ArithmeticExpression> get() = emptyList()
    override fun toString(): String = token.toString()
}

// I use AST node instead of List<Token> as return type as
// 1) It is more universal
// 2) I have to make AST anyway
// 3) It doesn't affect used patterns
fun parseExpression(tokens: List<TokenOrPar>, input: String): ArithmeticExpression {
    val parserState = ParserState(tokens, input)
    val expression = parserState.parseExpression()
    if (parserState.curIndex < parserState.list.size || expression == null)
        throw IllegalArgumentException("Expected EOS at position ${parserState.position + 1} but found '${parserState.input[parserState.position]}'")
    return expression
}

fun tokenizeAndParse(input: String) = parseExpression(tokenize(input), input)

private fun ParserState.parseExpression(): ArithmeticExpression? = parseSumsOrDiffs()
private fun <T : Token, L : ArithmeticExpression?> ParserState.whenToken(
    existing: L,
    argumentParser: ParserState.() -> ArithmeticExpression?,
    token: T,
    combiner: (T, L, ArithmeticExpression) -> ArithmeticExpression
): ArithmeticExpression {
    pop()
    val secondArg = argumentParser() ?: argumentNotFound(position - 1)
    return combiner(token, existing, secondArg)
}

private fun ParserState.argumentNotFound(position: Int): Nothing {
    val found = input.elementAtOrNull(position + 1)?.let { "'$it'" } ?: "EOS"
    throw IllegalArgumentException("Expected an argument for '${input[position]}' at position ${position + 2} but found $found")
}

private fun ParserState.parseSumsOrDiffs(): ArithmeticExpression? {
    var res = parseMultiOrDivs() ?: return null
    while (true) {
        res = when (val cur = peek()) {
            is Plus -> whenToken(res, { parseMultiOrDivs() }, cur, ::Sum)
            is Minus -> whenToken(res, { parseMultiOrDivs() }, cur, ::Subtraction)
            else -> return res
        }
    }
}

private fun ParserState.parseMultiOrDivs(): ArithmeticExpression? {
    var res = parseUnary() ?: return null
    while (true) {
        res = when (val cur = peek()) {
            is Asterisk -> whenToken(res, { parseUnary() }, cur, ::Multiplication)
            is Slash -> whenToken(res, { parseUnary() }, cur, ::Division)
            else -> return res
        }
    }
}

private fun ParserState.parseUnary(): ArithmeticExpression? = when (val cur = peek()) {
    is Plus -> whenToken(null, { parseUnary() }, cur) { token, _, x -> UnaryPlus(token, x) }
    is Minus -> whenToken(null, { parseUnary() }, cur) { token, _, x -> UnaryMinus(token, x) }
    else -> parseAtom()
}

private fun ParserState.parseAtom(): ArithmeticExpression? = when (val cur = peek()) {
    is Num -> ArithmeticExpressionFromNum(cur).also { pop() }
    is LPar -> {
        pop()
        val inner = parseExpression() ?: argumentNotFound(position - 1)
        val endPos = position
        val par = pop()
        if (par !is RPar)
            throw IllegalArgumentException(
                "Expected ')' at position ${endPos + 1} for '(' at position ${cur.position + 1} but found ${
                    input.elementAtOrNull(
                        endPos
                    ) ?: "EOS"
                }"
            )
        inner
    }
    else -> null
}
