package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import tachiyomi.domain.excerpt.model.Excerpt

@Serializable
data class BackupExcerpt(
    @ProtoNumber(1) var text: String,
    @ProtoNumber(2) var mangaTitle: String,
    @ProtoNumber(3) var chapterName: String,
    @ProtoNumber(4) var chapterNumber: Double = -1.0,
    @ProtoNumber(5) var sourceId: Long = -1L,
    @ProtoNumber(6) var mangaUrl: String = "",
    @ProtoNumber(7) var chapterUrl: String = "",
    @ProtoNumber(8) var createdAt: Long = 0L,
) {
    fun toExcerpt(): Excerpt {
        return Excerpt(
            text = text,
            mangaTitle = mangaTitle,
            chapterName = chapterName,
            chapterNumber = chapterNumber,
            sourceId = sourceId,
            mangaUrl = mangaUrl,
            chapterUrl = chapterUrl,
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
        sourceId = excerpt.sourceId,
        mangaUrl = excerpt.mangaUrl,
        chapterUrl = excerpt.chapterUrl,
        createdAt = excerpt.createdAt,
    )
}
