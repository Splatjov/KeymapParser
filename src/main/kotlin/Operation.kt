abstract class Operation {
    abstract val operation: (Expression, Expression) -> Int
}

object Plus: Operation(){
    override val operation = {a: Expression, b: Expression -> a.result + b.result}
}

object Minus: Operation(){
    override val operation = {a: Expression, b: Expression -> a.result - b.result}
}

object Multiply: Operation(){
    override val operation = {a: Expression, b: Expression -> a.result * b.result}
}

