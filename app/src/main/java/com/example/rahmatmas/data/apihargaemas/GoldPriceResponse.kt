package com.example.rahmatmas.data.apihargaemas

import com.google.gson.annotations.SerializedName

data class GoldPriceResponse(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("meta")
	val meta: Meta
)

data class DataItem(

	@field:SerializedName("unit")
	val unit: String,

	@field:SerializedName("sell")
	val sell: Int,

	@field:SerializedName("buy")
	val buy: Int,

	@field:SerializedName("weight")
	val weight: Any,

	@field:SerializedName("type")
	val type: String,

	@field:SerializedName("info")
	val info: String
)

data class Meta(

	@field:SerializedName("engine")
	val engine: String,

	@field:SerializedName("url")
	val url: String
)