package dev.anaes.qrh.model

/** A guideline's content as laid out: one item, or a fork of alternative branches. */
sealed interface ContentBlock {

    /** Content indices in this block, in order. */
    val indices: List<Int>

    data class Single(val index: Int) : ContentBlock {
        override val indices: List<Int> get() = listOf(index)
    }

    /** Alternative paths ("If AIRWAY fire" / "If NON-AIRWAY fire"), shown together. */
    data class Fork(val branches: List<Branch>) : ContentBlock {
        override val indices: List<Int> get() = branches.flatMap { listOf(it.headerIndex) + it.itemIndices }
    }

    data class Branch(val id: String, val headerIndex: Int, val itemIndices: List<Int>)
}

/**
 * Groups [content] into blocks: consecutive branches (each a [ContentType.BRANCH] header followed
 * by the items tagged with its id) form a fork; everything else is a single item. Items for which
 * [include] is false are left out, but branch headers are always kept.
 */
fun contentBlocks(content: List<ContentItem>, include: (Int) -> Boolean = { true }): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    var i = 0
    while (i < content.size) {
        if (content[i].type != ContentType.BRANCH) {
            if (include(i)) blocks.add(ContentBlock.Single(i))
            i++
            continue
        }
        val branches = mutableListOf<ContentBlock.Branch>()
        while (i < content.size && content[i].type == ContentType.BRANCH) {
            val header = i
            val id = content[header].branch
            val items = mutableListOf<Int>()
            i++
            while (i < content.size && content[i].type != ContentType.BRANCH && id.isNotEmpty() && content[i].branch == id) {
                if (include(i)) items.add(i)
                i++
            }
            branches.add(ContentBlock.Branch(id, header, items))
        }
        blocks.add(ContentBlock.Fork(branches))
    }
    return blocks
}

/** Index of the block holding the content item at [contentIndex], or -1. */
fun List<ContentBlock>.blockIndexOf(contentIndex: Int): Int = indexOfFirst { contentIndex in it.indices }

/** The condition of the branch [item] is in ("If circulatory arrest"), as HTML, or null. */
fun List<ContentItem>.branchCondition(item: ContentItem): String? {
    if (item.branch.isEmpty()) return null
    return firstOrNull { it.type == ContentType.BRANCH && it.branch == item.branch }?.body
}
