package com.example.rahmatmas.data.repository

import com.example.rahmatmas.data.apihargaemas.ApiConfig
import com.example.rahmatmas.data.apihargaemas.GoldPriceResponse
import retrofit2.Response

class GoldPriceRepository {
    suspend fun getGoldPrice(): Response<GoldPriceResponse> {
        return ApiConfig.getApiService().getHargaEmas()
    }
}