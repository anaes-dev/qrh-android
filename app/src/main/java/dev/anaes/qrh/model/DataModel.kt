package dev.anaes.qrh.model
import androidx.compose.ui.text.AnnotatedString
import com.google.gson.annotations.SerializedName

data class DataModel (
    @SerializedName("version")
    var version: String,
    @SerializedName("data")
    var guidelines: ArrayList<Guideline>
    )

data class Guideline (
    @SerializedName("title")
    var title: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("version")
    val version: Int,

    @SerializedName("url")
    val url: String,

    @SerializedName("content")
    var content: ArrayList<Item>,

    var titleA: AnnotatedString,

    var codeA: AnnotatedString,
)

data class Item (
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