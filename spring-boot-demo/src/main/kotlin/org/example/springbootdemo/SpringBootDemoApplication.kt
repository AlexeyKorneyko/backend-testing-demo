package org.example.springbootdemo

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.math.BigDecimal

fun main(args: Array<String>) {
    runApplication<SpringBootDemoApplication>(*args)
}

@SpringBootApplication
class SpringBootDemoApplication {

    @Bean
    fun webClient(@Value("\${priceApiUrl}") priceApiUrl: String): WebClient =
        WebClient.create(priceApiUrl)
}

data class Product(
    val id: String,
    val averagePrice: BigDecimal,
)

@RestController
class SpringController(
    private val service: SpringService,
) {
    @GetMapping("product/{id}")
    suspend fun getProduct(@PathVariable id: String) = service.getProduct(id)
}

@Service
class SpringService(
    private val priceApi: PriceApi,
) {
    suspend fun getProduct(id: String): Product = Product(id, priceApi.getAveragePrice(id))
}

interface PriceApi {
    suspend fun getAveragePrice(id: String): BigDecimal
}

@Service
class PriceApiOverHttp(
    private val webClient: WebClient,
) : PriceApi {
    override suspend fun getAveragePrice(id: String): BigDecimal {
        return webClient.get().uri("/price/$id").retrieve().awaitBody<PriceDto>().price
    }

    data class PriceDto(val price: BigDecimal)
}
