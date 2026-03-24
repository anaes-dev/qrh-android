package dev.anaes.qrh.model

import kotlinx.serialization.Serializable

@Serializable
data class AppData(
    val version: String,
    val data: List<Guideline>
)

@Serializable
data class Guideline(
    val title: String,
    val code: String,
    val version: Int,
    val url: String,
    val content: List<ContentItem>
)

@Serializable
data class ContentItem(
    val type: Int,
    val step: String = "",
    val head: String = "",
    val body: String = ""
)
