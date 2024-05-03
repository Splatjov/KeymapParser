open class ParserException(message: String): RuntimeException(message)

class NoNullExpressionException(index: Int) : ParserException("Expression at index $index is null")

class ExpectationsRuinedException(index: Int) : ParserException("Expected expression to end at $index")

class NotExpressionException(type: String, index: Int = 0) : ParserException("Not a $type expression at index $index, but expected to be")