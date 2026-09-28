package tachiyomi.domain.excerpt.model

/**
 * A reader-kept passage (Excerpt Vault, issue #53).
 *
 * Novel/chapter titles and urls are denormalized so an excerpt stays readable even when
 * the novel is not (or no longer) in the library. The nullable [mangaId]/[chapterId]
 * allow jumping back to the source chapter while it is still available.
 */
data class Excerpt(
    val id: Long,
    val text: String,
    val mangaTitle: String,
    val chapterName: String,
    val chapterNumber: Double,
    val mangaId: Long?,
    val chapterId: Long?,
    val sourceId: Long,
    val mangaUrl: String,
    val chapterUrl: String,
    val category: String,
    val note: String,
    val createdAt: Long,
) {
    companion object {
        fun create(
            text: String,
            mangaTitle: String,
            chapterName: String,
            chapterNumber: Double = -1.0,
            mangaId: Long? = null,
            chapterId: Long? = null,
            sourceId: Long = -1L,
            mangaUrl: String = "",
            chapterUrl: String = "",
            category: String = "",
            note: String = "",
            createdAt: Long = System.currentTimeMillis(),
        ) = Excerpt(
            id = -1L,
            text = text,
            mangaTitle = mangaTitle,
            chapterName = chapterName,
            chapterNumber = chapterNumber,
            mangaId = mangaId,
            chapterId = chapterId,
            sourceId = sourceId,
            mangaUrl = mangaUrl,
            chapterUrl = chapterUrl,
            category = category,
            note = note,
            createdAt = createdAt,
        )
    }
}
