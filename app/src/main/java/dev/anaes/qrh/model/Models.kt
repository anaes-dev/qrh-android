package dev.anaes.qrh.model

import kotlinx.serialization.Serializable

@Serializable
data class AppData(
    val version: String,
    /** Copyright holders, keyed by the id each guideline's [Copyright.holder] refers to. */
    val copyrightHolders: Map<String, CopyrightHolder> = emptyMap(),
    val data: List<Guideline>
)

@Serializable
data class CopyrightHolder(
    val name: String,
    val website: String,
)

@Serializable
data class Copyright(
    /** Key into [AppData.copyrightHolders]. */
    val holder: String,
    val year: Int,
)

@Serializable
data class Guideline(
    val title: String,
    val code: String,
    val version: Int,
    val url: String,
    val content: List<ContentItem>,
    val copyright: Copyright? = null,
)

@Serializable
data class ContentItem(
    val type: Int,
    val step: String = "",
    val head: String = "",
    val body: String = ""
)

/** A guideline's copyright with its holder looked up, ready for display. */
data class GuidelineCopyright(
    val holderName: String,
    val website: String,
    val year: Int,
)
