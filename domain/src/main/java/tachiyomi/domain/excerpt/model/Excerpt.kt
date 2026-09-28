package tachiyomi.domain.excerpt.model

/**
 * A reader-kept passage (Excerpt Vault, issue #53).
 *
 * Novel and chapter are stored by title and url rather than id so an excerpt stays
 * readable, and can still find its chapter, whether or not the novel is in the library.
 */
data class Excerpt(
    val id: Long = -1L,
    val text: String,
    val mangaTitle: String,
    val chapterName: String,
    val chapterNumber: Double,
    val sourceId: Long,
    val mangaUrl: String,
    val chapterUrl: String,
    val createdAt: Long,
)
