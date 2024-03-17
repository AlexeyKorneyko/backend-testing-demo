package org.example.springbootdemo

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles(value = ["fakes"])
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
@Profile("fakes")
class TestConfig {

    @Bean
    @Primary
    fun fakePriceApi(): FakePriceApi = FakePriceApi()
}

class FakePriceApi : PriceApi {

    val priceMap = mutableMapOf<String, BigDecimal>()

    override suspend fun getAveragePrice(id: String) = priceMap.getValue(id)
}

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 9999)
@ActiveProfiles(value = ["test"])
class ControllerIntegrationTest(
    private val webTestClient: WebTestClient,
) : StringSpec({

    val id = "sample"
    stubFor(
        get(urlEqualTo("/price/$id")).willReturn(
            aResponse().withHeader("Content-Type", "application/json")
                .withBody("""{"price": 10.0}""")
        )
    )

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
