package org.example

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.bigdecimal.shouldBeEqualIgnoringScale
import io.kotest.matchers.shouldBe
import org.example.PriceApiOverHttp.PriceDto
import org.http4k.client.HelidonClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Helidon
import org.http4k.server.asServer
import java.math.BigDecimal

class ApplicationKtTest : StringSpec({
    val fake = FakePriceApi()
    val priceApi = PriceApiOverHttp(priceApplication(fake))
    val productService = ProductService(priceApi)

    val testApp = application(productService)

    testSpec(testApp) { id, price -> fake.priceMap[id] = price }()
})

class ApplicationIntegrationTest : StringSpec({
    val fake = FakePriceApi()
    val priceServer = priceApplication(fake).asServer(Helidon(9000))

    beforeSpec {
        priceServer.start()
    }
    afterSpec {
        priceServer.stop()
    }
    val priceApi = PriceApiOverHttp(SetBaseUriFrom(Uri.of("http://localhost:9000")).then(HelidonClient()))
    val productService = ProductService(priceApi)

    val testApp = application(productService)

    testSpec(testApp) { id, price -> fake.priceMap[id] = price }()
})

class FakePriceApi {

    val priceMap = mutableMapOf<String, BigDecimal>()

    fun getAveragePrice(id: String) = priceMap.getValue(id)
}

fun priceApplication(service: FakePriceApi): HttpHandler =
    routes(
        "/price/{id}" bind Method.GET to { request ->
            request.path("id")?.let {
                Response(OK).with(priceDtoLens of PriceDto(service.getAveragePrice(it)))
            } ?: Response(Status.BAD_REQUEST)
        },
    )

private fun testSpec(httpClient: HttpHandler, priceSetup: (String, BigDecimal) -> Unit): StringSpec.() -> Unit =
    {
        val id = "sample"
        priceSetup(id, BigDecimal.TEN)

        "should return product" {
            httpClient(Request(Method.GET, "/product/$id")).apply {
                this shouldHaveStatus OK
                productLens(this).id shouldBe id
                productLens(this).averagePrice shouldBeEqualIgnoringScale BigDecimal.TEN
            }
        }
    }
