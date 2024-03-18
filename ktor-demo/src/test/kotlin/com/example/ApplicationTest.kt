package com.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.TestApplication
import java.math.BigDecimal

class ApplicationTest : StringSpec({
    val priceApi = FakePriceApi()
    val testApp = TestApplication {
        application {
            module(ProductService(priceApi))
        }
    }

    beforeSpec {
        testApp.start()
    }
    afterSpec {
        testApp.stop()
    }

    val client = testApp.createClient {
        install(ContentNegotiation) {
            jackson()
        }
    }

    testSpec(client) { id, price -> priceApi.priceMap[id] = price }()
})

class ApplicationTestWithMockEngine : StringSpec({
    val priceApi = FakePriceApi()

    val priceApiOverHttp = PriceApiOverHttp(
        priceApi.httpClient,
    )
    val testApp = TestApplication {
        application {
            module(ProductService(priceApiOverHttp))
        }
    }

    beforeSpec {
        testApp.start()
    }
    afterSpec {
        testApp.stop()
    }

    val client = testApp.createClient {
        install(ContentNegotiation) {
            jackson()
        }
    }

    testSpec(client) { id, price -> priceApi.priceMap[id] = price }()
})

class FakePriceApi : PriceApi {

    val priceMap = mutableMapOf<String, BigDecimal>()

    private val engine = MockEngine { request ->
        val id = request.url.pathSegments.last()
        respond(
            content = jacksonObjectMapper().writeValueAsBytes(PriceApiOverHttp.PriceDto(getAveragePrice(id))),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

    val httpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    override suspend fun getAveragePrice(id: String) = priceMap.getValue(id)
}

private fun testSpec(httpClient: HttpClient, priceSetup: (String, BigDecimal) -> Unit): StringSpec.() -> Unit =
    {
        val id = "sample"
        priceSetup(id, BigDecimal.TEN)

        "should return product" {
            println("request")
            httpClient.get("/product/$id").apply {
                status shouldBe HttpStatusCode.OK
                body<Product>().apply {
                    this.id shouldBe id
                    this.averagePrice shouldBe BigDecimal.TEN
                }
            }
        }
    }
