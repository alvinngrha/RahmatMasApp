package com.example.rahmatmas.data.apihargaemas

import com.google.gson.annotations.SerializedName

data class GoldPriceResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("data")
    val data: GoldPriceData?
)

data class GoldPriceData(
    @SerializedName("timestamp")
    val timestamp: Long?,
    @SerializedName("base_currency")
    val baseCurrency: String?,
    @SerializedName("metals")
    val metals: String?,
    @SerializedName("weight_unit")
    val weightUnit: String?,
    @SerializedName("weight_name")
    val weightName: String?,
    @SerializedName("metal_prices")
    val metalPrices: Map<String, MetalPrice>?,
    @SerializedName("currency_rates")
    val currencyRates: Map<String, Double>?
)

data class MetalPrice(
    @SerializedName("open")
    val open: Double?,
    @SerializedName("high")
    val high: Double?,
    @SerializedName("low")
    val low: Double?,
    @SerializedName("prev")
    val prev: Double?,
    @SerializedName("change")
    val change: Double?,
    @SerializedName("change_percentage")
    val changePercentage: Double?,
    @SerializedName("price")
    val price: Double?,
    @SerializedName("ask")
    val ask: Double?,
    @SerializedName("bid")
    val bid: Double?,
    @SerializedName("price_24k")
    val price24k: Double?,
    @SerializedName("price_22k")
    val price22k: Double?,
    @SerializedName("price_21k")
    val price21k: Double?,
    @SerializedName("price_20k")
    val price20k: Double?,
    @SerializedName("price_18k")
    val price18k: Double?,
    @SerializedName("price_16k")
    val price16k: Double?,
    @SerializedName("price_14k")
    val price14k: Double?,
    @SerializedName("price_10k")
    val price10k: Double?
)
