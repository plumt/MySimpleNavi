package com.yun.mysimplenavi.data.model

import com.google.gson.annotations.SerializedName
import com.yun.mysimplenavi.base.Item

class KeywordSearchModel {
    data class RS(
        val documents: ArrayList<Documents>?,
        val meta: Meta?
    ){
        data class Documents(
            override var id: Int,
            val address_name: String,
            val road_address_name: String,
            val place_name: String,
            @SerializedName("x") val lon: String,
            @SerializedName("y") val lat: String
        ) : Item()
        data class Meta(
            val is_end: Boolean,
            val total_count: Int
        )
    }
}