import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParserTest {
    @Test
    fun testCorrect() {
        val correctTestsMap = mapOf(
            Pair("1", 1),
            Pair("element", elementResult),
            Pair("(1+2)", 3),
            Pair("(-100--100)", 0),
            Pair("(((225+1)+1)+1)", 228),
            Pair("(-1000*5)", -5000)
        )

        for (pair in correctTestsMap.entries) {
            println("TEST ${pair.key}")
            assertEquals(
                pair.key.parseExpression().result,
                pair.value,
                "in string ${pair.key} expected ${pair.value} got ${pair.key.parseExpression().result}"
            )
        }
    }

    @Test
    fun testIncorrect() {
        val incorrectTestsMap = mapOf(
            Pair("fs", NotExpressionException::class),
            Pair("", NoNullExpressionException::class),
            Pair("(1+2+3)", ExpectationsRuinedException::class),
            Pair("(1+)", NoNullExpressionException::class),
            Pair("(((+3)", NotExpressionException::class)
        )

        for (pair in incorrectTestsMap.entries) {
            println("TEST ${pair.key}, should fall with ${pair.value.simpleName}")
            assertFailsWith(pair.value) { pair.key.parseExpression() }
        }
    }

}