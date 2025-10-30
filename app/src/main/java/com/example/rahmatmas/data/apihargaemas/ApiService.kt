package com.example.rahmatmas.data.apihargaemas

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v1/latest")
    suspend fun getHargaEmas(
        @Query("metals") metals: String = "XAU,XAG,XPT,XPD",
        @Query("base_currency") baseCurrency: String = "IDR",
        @Query("currencies") currencies: String = "IDR",
        @Query("weight_unit") weightUnit: String = "gram"
    ): Response<GoldPriceResponse>
}
