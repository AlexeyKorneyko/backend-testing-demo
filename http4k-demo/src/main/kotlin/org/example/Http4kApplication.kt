package org.example

import org.http4k.client.HelidonClient
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Helidon
import org.http4k.server.asServer
import java.math.BigDecimal

fun main() {
    val client = SetBaseUriFrom(Uri.of("https://example.org")).then(HelidonClient())
    val priceApi = PriceApiOverHttp(client)
    val productService = ProductService(priceApi)

    application(productService).asServer(Helidon()).start()
}

fun application(serice: ProductService): HttpHandler = routes(
    "/product/{id}" bind GET to { request ->
        request.path("id")?.let {
            Response(OK).with(productLens of serice.getProduct(it))
        } ?: Response(BAD_REQUEST)
    },
)

data class Product(
    val id: String,
    val averagePrice: BigDecimal,
)

val productLens = Body.auto<Product>().toLens()

class ProductService(
    private val priceApi: PriceApi,
) {
    fun getProduct(id: String): Product = Product(id, priceApi.getAveragePrice(id))
}

interface PriceApi {
    fun getAveragePrice(id: String): BigDecimal
}

class PriceApiOverHttp(
    private val webClient: HttpHandler,
) : PriceApi {
    override fun getAveragePrice(id: String): BigDecimal {
        val response = webClient(Request(GET, "/price/$id"))
        return priceDtoLens(response).price
    }

    data class PriceDto(val price: BigDecimal)
}

val priceDtoLens = Body.auto<PriceApiOverHttp.PriceDto>().toLens()
