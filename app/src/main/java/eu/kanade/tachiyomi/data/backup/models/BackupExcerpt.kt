package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import tachiyomi.domain.excerpt.model.Excerpt

/**
 * Portable form of an Excerpt Vault entry (issue #53). Ids are stored as -1 when the
 * novel/chapter reference is absent so old and new backups stay plain protobuf.
 */
@Serializable
data class BackupExcerpt(
    @ProtoNumber(1) var text: String,
    @ProtoNumber(2) var mangaTitle: String,
    @ProtoNumber(3) var chapterName: String,
    @ProtoNumber(4) var chapterNumber: Double = -1.0,
    @ProtoNumber(5) var mangaId: Long = -1L,
    @ProtoNumber(6) var chapterId: Long = -1L,
    @ProtoNumber(7) var sourceId: Long = -1L,
    @ProtoNumber(8) var mangaUrl: String = "",
    @ProtoNumber(9) var chapterUrl: String = "",
    @ProtoNumber(10) var category: String = "",
    @ProtoNumber(11) var note: String = "",
    @ProtoNumber(12) var createdAt: Long = 0L,
) {
    fun toExcerpt(): Excerpt {
        return Excerpt(
            id = -1L,
            text = text,
            mangaTitle = mangaTitle,
            chapterName = chapterName,
            chapterNumber = chapterNumber,
            mangaId = mangaId.takeIf { it >= 0 },
            chapterId = chapterId.takeIf { it >= 0 },
            sourceId = sourceId,
            mangaUrl = mangaUrl,
            chapterUrl = chapterUrl,
            category = category,
            note = note,
            createdAt = createdAt,
        )
    }
}

val backupExcerptMapper = { excerpt: Excerpt ->
    BackupExcerpt(
        text = excerpt.text,
        mangaTitle = excerpt.mangaTitle,
        chapterName = excerpt.chapterName,
        chapterNumber = excerpt.chapterNumber,
        mangaId = excerpt.mangaId ?: -1L,
        chapterId = excerpt.chapterId ?: -1L,
        sourceId = excerpt.sourceId,
        mangaUrl = excerpt.mangaUrl,
        chapterUrl = excerpt.chapterUrl,
        category = excerpt.category,
        note = excerpt.note,
        createdAt = excerpt.createdAt,
    )
}
