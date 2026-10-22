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
    val body: String = "",
    /**
     * Where a guideline forks ("If circulatory arrest" / "If no circulatory arrest"), the branch
     * this item is in: the same id as its [ContentType.BRANCH] header. Empty outside a branch.
     */
    val branch: String = "",
    /**
     * A box's content as a table, drawn instead of [body]. The body still holds the same content
     * as text, which search uses, and which apps that predate tables show instead.
     */
    val table: ContentTable? = null,
)

/** A table in a box: column headings, then rows whose first cell names the row. */
@Serializable
data class ContentTable(
    val columns: List<String>,
    val rows: List<List<String>>,
)

/** [ContentItem.type] values the app treats specially. */
object ContentType {
    /** The START banner, and section banners such as "ONGOING MANAGEMENT:". */
    const val BANNER = 2
    const val IMAGE = 10
    /** The condition heading a branch of a fork ("If circulatory arrest:"). */
    const val BRANCH = 13
}

/** A guideline's copyright with its holder looked up, ready for display. */
data class GuidelineCopyright(
    val holderName: String,
    val website: String,
    val year: Int,
)
