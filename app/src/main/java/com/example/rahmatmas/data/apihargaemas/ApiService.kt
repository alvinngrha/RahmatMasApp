package com.example.rahmatmas.data.apihargaemas

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("prices/anekalogam")
    suspend fun getHargaEmas(): Response<GoldPriceResponse>
}