package visitors

import ArithmeticExpression
import ArithmeticExpressionFromNum
import Division
import ExpressionVisitor
import Multiplication
import Subtraction
import Sum
import UnaryMinus
import UnaryPlus

private object EvaluatorVisitor : ExpressionVisitor<Double> {
    override fun visit(expression: ArithmeticExpression): Double = when (expression) {
        is ArithmeticExpressionFromNum -> expression.token.value
        is Division -> visit(expression.arg1) / visit(expression.arg2)
        is Multiplication -> visit(expression.arg1) * visit(expression.arg2)
        is Subtraction -> visit(expression.arg1) - visit(expression.arg2)
        is Sum -> visit(expression.arg1) + visit(expression.arg2)
        is UnaryMinus -> -visit(expression.arg)
        is UnaryPlus -> visit(expression.arg)
    }
}
fun evaluate(expression: ArithmeticExpression): Double = expression.accept(EvaluatorVisitor)