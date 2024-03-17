import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MainKtTest {

    @Test
    fun test() {

        // kotlin.test
        assertEquals(4, 2 + 2)
        assertThat("10.0".toBigDecimal().compareTo(BigDecimal.TEN) == 0)
        assertContentEquals(listOf("a", "b", "c").sorted(), listOf("c", "a", "b").sorted())

        // Assertj
        assertThat(2 + 2).isEqualTo(4)
        assertThat("10.0".toBigDecimal()).isEqualByComparingTo(BigDecimal.TEN)
        assertThat(listOf("a", "b", "c")).containsExactlyInAnyOrder("c", "a", "b")

        // kotest-assertions
        2 + 2 shouldBe 4
        "10.0".toBigDecimal() shouldBeEqualComparingTo BigDecimal.TEN
        listOf("a", "b", "c").shouldContainExactlyInAnyOrder("c", "a", "b")
    }

}
