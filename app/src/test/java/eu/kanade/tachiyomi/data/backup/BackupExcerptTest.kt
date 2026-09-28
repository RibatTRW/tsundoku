package eu.kanade.tachiyomi.data.backup

import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.backup.models.backupExcerptMapper
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tachiyomi.domain.excerpt.model.Excerpt

class BackupExcerptTest {

    @Test
    fun `excerpts survive a backup encode decode round-trip`() {
        val excerpt = Excerpt(
            text = "It was a bright cold day in April, and the clocks were striking thirteen.",
            mangaTitle = "1984",
            chapterName = "Chapter 1",
            chapterNumber = 1.0,
            sourceId = 3L,
            mangaUrl = "/novel/1984",
            chapterUrl = "/novel/1984/chapter-1",
            createdAt = 1_700_000_000_000L,
        )
        val backup = Backup(backupManga = emptyList(), backupExcerpts = listOf(backupExcerptMapper(excerpt)))

        val decoded = ProtoBuf.decodeFromByteArray(
            Backup.serializer(),
            ProtoBuf.encodeToByteArray(Backup.serializer(), backup),
        )

        assertEquals(listOf(excerpt), decoded.backupExcerpts.map { it.toExcerpt() })
    }

    @Test
    fun `backup without excerpts decodes with an empty excerpt list`() {
        val bytes = ProtoBuf.encodeToByteArray(Backup.serializer(), Backup(backupManga = emptyList()))

        assertTrue(ProtoBuf.decodeFromByteArray(Backup.serializer(), bytes).backupExcerpts.isEmpty())
    }
}
