package eu.kanade.tachiyomi.data.backup

import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.backup.models.BackupExcerpt
import eu.kanade.tachiyomi.data.backup.models.backupExcerptMapper
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tachiyomi.domain.excerpt.model.Excerpt

class BackupExcerptTest {

    private val excerpt = Excerpt.create(
        text = "It was a bright cold day in April, and the clocks were striking thirteen.",
        mangaTitle = "1984",
        chapterName = "Chapter 1",
        chapterNumber = 1.0,
        mangaId = 42L,
        chapterId = 7L,
        sourceId = 3L,
        mangaUrl = "/novel/1984",
        chapterUrl = "/novel/1984/chapter-1",
        category = "wisdom",
        note = "opening line",
        createdAt = 1_700_000_000_000L,
    )

    @Test
    fun `excerpts survive a backup encode decode round-trip`() {
        val backup = Backup(
            backupManga = emptyList(),
            backupExcerpts = listOf(backupExcerptMapper(excerpt)),
        )
        val decoded = ProtoBuf.decodeFromByteArray(
            Backup.serializer(),
            ProtoBuf.encodeToByteArray(Backup.serializer(), backup),
        )

        assertEquals(1, decoded.backupExcerpts.size)
        val restored = decoded.backupExcerpts[0].toExcerpt()
        assertEquals(excerpt.text, restored.text)
        assertEquals(excerpt.mangaTitle, restored.mangaTitle)
        assertEquals(excerpt.chapterName, restored.chapterName)
        assertEquals(excerpt.chapterNumber, restored.chapterNumber)
        assertEquals(excerpt.mangaId, restored.mangaId)
        assertEquals(excerpt.chapterId, restored.chapterId)
        assertEquals(excerpt.sourceId, restored.sourceId)
        assertEquals(excerpt.mangaUrl, restored.mangaUrl)
        assertEquals(excerpt.chapterUrl, restored.chapterUrl)
        assertEquals(excerpt.category, restored.category)
        assertEquals(excerpt.note, restored.note)
        assertEquals(excerpt.createdAt, restored.createdAt)
    }

    @Test
    fun `excerpts without stored ids restore with null ids`() {
        val backupExcerpt = BackupExcerpt(
            text = "Some passage",
            mangaTitle = "Untracked Novel",
            chapterName = "Chapter 5",
        )
        val restored = backupExcerpt.toExcerpt()

        assertNull(restored.mangaId)
        assertNull(restored.chapterId)
        assertEquals("Some passage", restored.text)
        assertEquals("Untracked Novel", restored.mangaTitle)
    }

    @Test
    fun `backup without excerpts decodes with an empty excerpt list`() {
        val bytes = ProtoBuf.encodeToByteArray(Backup.serializer(), Backup(backupManga = emptyList()))
        val decoded = ProtoBuf.decodeFromByteArray(Backup.serializer(), bytes)

        assertTrue(decoded.backupExcerpts.isEmpty())
        assertTrue(decoded.backupManga.isEmpty())
    }
}
