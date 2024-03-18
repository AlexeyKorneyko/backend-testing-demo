package com.example

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import java.math.BigDecimal

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        val priceApi = PriceApiOverHttp(HttpClient(CIO.create()) { defaultRequest { host = "example.org" } })
        val productService = ProductService(priceApi)
        module(productService)
    }.start(wait = true)

}

fun Application.module(productService: ProductService) {
    configureRouting(productService)
    install(ContentNegotiation) {
        jackson()
    }
}

fun Application.configureRouting(service: ProductService) {
    routing {
        get("/product/{id}") {
            call.respond(
                service.getProduct(call.parameters.getOrFail("id"))
            )
        }
    }
}

data class Product(
    val id: String,
    val averagePrice: BigDecimal,
)

class ProductService(
    private val priceApi: PriceApi,
) {
    suspend fun getProduct(id: String): Product = Product(id, priceApi.getAveragePrice(id))
}

interface PriceApi {
    suspend fun getAveragePrice(id: String): BigDecimal
}

class PriceApiOverHttp(
    private val webClient: HttpClient,
) : PriceApi {
    override suspend fun getAveragePrice(id: String): BigDecimal {
        return webClient.get("/price/$id").body<PriceDto>().price
    }

    data class PriceDto(val price: BigDecimal)
}
