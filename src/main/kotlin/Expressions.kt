import kotlinx.coroutines.*
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.primaryConstructor

abstract class Expression(str: String, start: Int = 0, end: Int = str.length) : Object(start, end) {
    abstract var result: Int
    override val length: Int = end - start

    val castPrimitive =
        mapOf(Pair(Primitives.PLUS, Plus), Pair(Primitives.MINUS, Minus), Pair(Primitives.MULTIPLY, Multiply))

    init {
        if (end-start == 0) throw NoNullExpressionException(start)
    }
}

class Element(str: String, start: Int = 0, end: Int = str.length) : Expression(str, start, end) {
    override var result: Int = elementResult

    init {
        if (!str.startsWith(
                Primitives.ELEMENT.value,
                start
            ) || Primitives.ELEMENT.value.length != end - start
        ) throw NotExpressionException("element", start)
    }
}

class ConstantExpression(str: String,  start: Int = 0, end: Int = str.length) : Expression(str, start, end) {
    override var result: Int

    init {
        result = str.substring(start, end).toIntOrNull() ?: throw NotExpressionException("constant", start)
    }
}

class BinaryExpression(str: String, start: Int = 0, end: Int = str.length) : Expression(str, start, end) {
    override var result: Int
    var firstExpression: Expression? = null
    var secondExpression: Expression? = null
    lateinit var binaryOperation: Operation

    init {
        val coScope = CoroutineScope(Dispatchers.Default)
        var currentStart = start
        var currentEnd = end
        var firstJob: Deferred<Expression>? = null
        var secondJob: Deferred<Expression>? = null

        fun takeConstant(): Expression {
            var sz = 0
            for (ind in currentStart..<currentEnd) {
                if (sz == 0 && str.startsWith(Primitives.MINUS.value, ind)) sz++
                else if (str[ind].isDigit()) sz++
                else break
            }
            val expression = ConstantExpression(str, currentStart, currentStart + sz)
            currentStart += sz
            return expression
        }

        fun takeElement(): Expression {
            val expression = Element(str, currentStart, currentStart + Primitives.ELEMENT.value.length)
            currentStart += Primitives.ELEMENT.value.length
            return expression
        }

        if (!str.startsWith(Primitives.PREFIX.value, currentStart) ||
            !str.startsWith(Primitives.SUFFIX.value, currentEnd - Primitives.SUFFIX.value.length)
        )
            throw NotExpressionException("binary", currentStart)

        currentStart += Primitives.PREFIX.value.length
        currentEnd -= Primitives.SUFFIX.value.length

        if (str.startsWith(Primitives.PREFIX.value, currentStart)) {
            var balance = 0
            for (ind in currentStart..<currentEnd) {
                if (str.startsWith(Primitives.PREFIX.value, ind)) balance += 1
                if (str.startsWith(Primitives.SUFFIX.value, ind)) balance -= 1
                if (balance == 0) {
                    val par1 = currentStart
                    val par2 = ind + Primitives.SUFFIX.value.length
                    if (coroutineMode) {
                        firstJob = coScope.async {
                            BinaryExpression(str, par1, par2)
                        }
                    } else firstExpression = BinaryExpression(str, par1, par2)

                    currentStart = ind + Primitives.SUFFIX.value.length
                    break
                }
            }
            if (firstJob == null) throw NotExpressionException("binary", currentStart)
        } else if (str.startsWith(Primitives.ELEMENT.value, currentStart)) firstExpression = takeElement()
        else firstExpression = takeConstant()

        for (operation in castPrimitive.entries) {
            if (str.startsWith(operation.key.value, currentStart)) {
                binaryOperation = operation.value
                currentStart += operation.key.value.length
                break
            }
        }

        if (str.startsWith(Primitives.PREFIX.value, currentStart)) {
            val par1 = currentStart
            val par2 = currentEnd
            if (coroutineMode) {
                secondJob = coScope.async { BinaryExpression(str, par1, par2) }
            } else secondExpression = BinaryExpression(str, par1, par2)
            currentStart = currentEnd
        } else if (str.startsWith(Primitives.ELEMENT.value, currentStart)) secondExpression = takeElement()
        else secondExpression = takeConstant()

        if (currentStart != currentEnd)
            throw ExpectationsRuinedException(currentStart)

        runBlocking {
            firstExpression = firstJob?.await() ?: firstExpression
            secondExpression = secondJob?.await() ?: secondExpression
        }

        result = binaryOperation.operation(
            firstExpression ?: throw NoNullExpressionException(start),
            secondExpression ?: throw NoNullExpressionException(start)
        )
    }
}

fun String.parseExpression() : Expression {
    var result: Expression
    for (expression in arrayOf(BinaryExpression::class, Element::class, ConstantExpression::class)) {
        try {
            result = expression.primaryConstructor?.call(this, 0, this.length) ?: continue
            return result
        } catch (err: InvocationTargetException) {
            if (err.targetException is NotExpressionException) continue
            throw err.targetException
        }

    }
    throw NotExpressionException("general")
}