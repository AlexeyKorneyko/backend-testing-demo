package org.example.springbootdemo

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureWebTestClient
class ControllerTest(
    private val webTestClient: WebTestClient,
    private val fakePriceApi: FakePriceApi,
) : StringSpec({

    val id = "sample"
    fakePriceApi.priceMap[id] = BigDecimal.TEN

    "should return average price" {
        webTestClient.get()
            .uri("/product/$id")
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(Product::class.java)
            .value {
                it.id shouldBe id
                it.averagePrice shouldBeEqualComparingTo BigDecimal.TEN
            }
    }
})

@Configuration
class TestConfig {
    @Primary
    @Bean
    fun fakePriceApi(): PriceApi = FakePriceApi()
}

class FakePriceApi : PriceApi {

    val priceMap = mutableMapOf<String, BigDecimal>()

    override suspend fun getAveragePrice(id: String) = priceMap.getValue(id)
}
