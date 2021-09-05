package dev.anaes.qrh.model

import com.google.gson.annotations.SerializedName

data class ListModel (
        @SerializedName("guidelines")
        var items: List<ListItem>
)

data class ListItem (

        @SerializedName("title")
        val title: String,

        @SerializedName("code")
        val code: String,

        @SerializedName("version")
        val version: Int,

        @SerializedName("url")
        val url: String

)
