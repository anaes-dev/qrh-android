package dev.anaes.qrh.model

import androidx.compose.ui.text.AnnotatedString
import com.google.gson.annotations.SerializedName

data class ListModel (
        @SerializedName("guidelines")
        var items: List<ListItem>
)

data class ListItem (

        @SerializedName("title")
        var title: String,

        @SerializedName("code")
        val code: String,

        @SerializedName("version")
        val version: Int,

        @SerializedName("url")
        val url: String,

        var titleA: AnnotatedString,

        var codeA: AnnotatedString


)
