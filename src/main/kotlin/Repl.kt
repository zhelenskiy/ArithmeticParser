import visitors.evaluate
import visitors.polishNotation

private fun List<TokenOrPar>.toReadableString() =
    joinToString(" ") { if (it is Num) "NUM(${it.value})" else it.javaClass.name.uppercase() }

fun main() {
    while (true) {
        val input = readlnOrNull() ?: break
        try {
            val expr = tokenizeAndParse(input)
            println(polishNotation(expr).toReadableString())
            println(evaluate(expr))
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
    }
}