package dev.anaes.qrh.model

import com.google.gson.annotations.SerializedName

data class DetailModel (

    @SerializedName("DetailContent")
    var items: ArrayList<DetailItem>

)

data class DetailItem (
    @SerializedName("type")
    val type: Int,

    @SerializedName("step")
    val step: String,

    @SerializedName("head")
    val head: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("collapsed")
    var collapsed: Boolean,
)

