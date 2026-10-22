package dev.anaes.qrh.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentBlocksTest {

    private fun item(type: Int, branch: String = "", body: String = "") =
        ContentItem(type = type, body = body, branch = branch)

    // Shaped like 3-10: steps, then two branches each with a step and a box, then a shared step
    private val content = listOf(
        item(4),                                                         // 0
        item(ContentType.BRANCH, "arrest", "If circulatory arrest:"),    // 1
        item(4, "arrest"),                                               // 2
        item(6, "arrest"),                                               // 3
        item(ContentType.BRANCH, "no-arrest", "If no circulatory arrest:"), // 4
        item(4, "no-arrest"),                                            // 5
        item(6, "no-arrest"),                                            // 6
        item(4),                                                         // 7
        item(5),                                                         // 8
    )

    @Test
    fun `consecutive branches form one fork, and items after them rejoin`() {
        assertEquals(
            listOf(
                ContentBlock.Single(0),
                ContentBlock.Fork(
                    listOf(
                        ContentBlock.Branch("arrest", 1, listOf(2, 3)),
                        ContentBlock.Branch("no-arrest", 4, listOf(5, 6)),
                    )
                ),
                ContentBlock.Single(7),
                ContentBlock.Single(8),
            ),
            contentBlocks(content),
        )
    }

    @Test
    fun `excluded items are left out, but branch headers are kept`() {
        val blocks = contentBlocks(content) { content[it].type !in 5..10 }

        assertEquals(
            listOf(
                ContentBlock.Single(0),
                ContentBlock.Fork(
                    listOf(
                        ContentBlock.Branch("arrest", 1, listOf(2)),
                        ContentBlock.Branch("no-arrest", 4, listOf(5)),
                    )
                ),
                ContentBlock.Single(7),
            ),
            blocks,
        )
    }

    @Test
    fun `a branch ends at the first item not tagged with it`() {
        val blocks = contentBlocks(
            listOf(item(ContentType.BRANCH, "a"), item(4, "a"), item(4, "b"), item(4))
        )
        assertEquals(
            listOf(
                ContentBlock.Fork(listOf(ContentBlock.Branch("a", 0, listOf(1)))),
                ContentBlock.Single(2),
                ContentBlock.Single(3),
            ),
            blocks,
        )
    }

    @Test
    fun `block index of a content item`() {
        val blocks = contentBlocks(content)
        assertEquals(0, blocks.blockIndexOf(0))
        assertEquals(1, blocks.blockIndexOf(1))
        assertEquals(1, blocks.blockIndexOf(6))
        assertEquals(2, blocks.blockIndexOf(7))
        assertEquals(-1, blocks.blockIndexOf(42))
    }

    @Test
    fun `branch condition of an item`() {
        assertEquals("If no circulatory arrest:", content.branchCondition(content[5]))
        assertNull(content.branchCondition(content[7]))
    }

    @Test
    fun `guidelines without branches are all single items`() {
        val plain = listOf(item(1), item(2), item(4), item(5))
        assertEquals(plain.indices.map { ContentBlock.Single(it) }, contentBlocks(plain))
    }
}
