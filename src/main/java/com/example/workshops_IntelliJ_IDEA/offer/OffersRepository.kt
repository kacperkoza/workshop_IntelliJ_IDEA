package com.example.workshops_IntelliJ_IDEA.offer

interface OffersRepository {

    fun getOffers(): List<Offer>

    fun addOffer(accountId: String, offer: Offer)
}
