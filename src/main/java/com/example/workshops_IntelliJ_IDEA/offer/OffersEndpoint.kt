package com.example.workshops_IntelliJ_IDEA.offer

import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping
class OffersEndpoint(
    private val offersService: OffersService
) {

    @GetMapping("/offers")
    fun getOffers(
        @RequestParam("limit") limit: Int?,
        @RequestParam("offset") offset: Int?,
    ): Offers {
        val offers = offersService.getOffers(limit, offset)
        return Offers(offers)
    }

    @PostMapping("/users/{userId}/offers")
    fun addOffer(
        @PathVariable("userId") userId: String,
        @RequestBody offer: Offer
    ) {
        offersService.add(offer, userId)
    }
}

data class Offers(
    val offers: List<Offer>,
)

data class Offer(
    val id: Long,
    val title: String,
    val image: URI,
)
