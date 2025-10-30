package com.example.rahmatmas.data.repository

import com.example.rahmatmas.data.apihargaemas.ApiConfig
import com.example.rahmatmas.data.apihargaemas.GoldPriceResponse
import com.example.rahmatmas.data.apihargaemas.MetalPrice
import retrofit2.Response

class GoldPriceRepository {
    suspend fun getGoldPrice(): Response<GoldPriceResponse> {
        return ApiConfig.getApiService().getHargaEmas()
    }

    fun extractGoldMetalPrice(response: GoldPriceResponse?): MetalPrice? {
        return response
            ?.data
            ?.metalPrices
            ?.get(GOLD_METAL_CODE)
    }

    companion object {
        private const val GOLD_METAL_CODE = "XAU"
    }
}
