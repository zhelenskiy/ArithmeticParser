package visitors

import ArithmeticExpression
import ExpressionVisitor
import Token

private class PolishNotationVisitor(val res: MutableList<Token> = mutableListOf()) : ExpressionVisitor<List<Token>> {
    override fun visit(expression: ArithmeticExpression): List<Token> {
        expression.arguments.forEach { it.accept(this) }
        res.add(expression.token)
        return res
    }
}

fun polishNotation(expr: ArithmeticExpression): List<Token> = expr.accept(PolishNotationVisitor())