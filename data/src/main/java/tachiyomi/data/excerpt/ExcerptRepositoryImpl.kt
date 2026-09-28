package tachiyomi.data.excerpt

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.flow.Flow
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList
import tachiyomi.domain.excerpt.model.Excerpt
import tachiyomi.domain.excerpt.repository.ExcerptRepository

class ExcerptRepositoryImpl(
    private val database: Database,
) : ExcerptRepository {

    override fun getExcerptsAsFlow(): Flow<List<Excerpt>> {
        return database.excerptsQueries.getExcerpts(::mapExcerpt).subscribeToList()
    }

    override fun searchExcerptsAsFlow(query: String): Flow<List<Excerpt>> {
        return database.excerptsQueries.searchExcerpts(query.escapeLike(), ::mapExcerpt).subscribeToList()
    }

    override fun getCategoriesAsFlow(): Flow<List<String>> {
        return database.excerptsQueries.getExcerptCategories().subscribeToList()
    }

    override suspend fun getExcerpt(id: Long): Excerpt? {
        return database.excerptsQueries.getExcerpt(id, ::mapExcerpt).awaitAsOneOrNull()
    }

    override suspend fun getAll(): List<Excerpt> {
        return database.excerptsQueries.getExcerpts(::mapExcerpt).awaitAsList()
    }

    override suspend fun insert(excerpt: Excerpt): Long {
        database.excerptsQueries.insert(
            text = excerpt.text,
            mangaTitle = excerpt.mangaTitle,
            chapterName = excerpt.chapterName,
            chapterNumber = excerpt.chapterNumber,
            mangaId = excerpt.mangaId,
            chapterId = excerpt.chapterId,
            sourceId = excerpt.sourceId,
            mangaUrl = excerpt.mangaUrl,
            chapterUrl = excerpt.chapterUrl,
            category = excerpt.category,
            note = excerpt.note,
            createdAt = excerpt.createdAt,
        )
        return database.excerptsQueries.selectLastInsertedRowId().awaitAsOne()
    }

    override suspend fun delete(excerptId: Long) {
        database.excerptsQueries.delete(excerptId)
    }

    override suspend fun insertMissing(excerpts: List<Excerpt>): Int {
        var inserted = 0
        database.transaction {
            excerpts.forEach { excerpt ->
                val exists = database.excerptsQueries.findDuplicate(
                    text = excerpt.text,
                    mangaTitle = excerpt.mangaTitle,
                    chapterName = excerpt.chapterName,
                    createdAt = excerpt.createdAt,
                ).awaitAsOneOrNull() != null
                if (!exists) {
                    database.excerptsQueries.insert(
                        text = excerpt.text,
                        mangaTitle = excerpt.mangaTitle,
                        chapterName = excerpt.chapterName,
                        chapterNumber = excerpt.chapterNumber,
                        mangaId = excerpt.mangaId,
                        chapterId = excerpt.chapterId,
                        sourceId = excerpt.sourceId,
                        mangaUrl = excerpt.mangaUrl,
                        chapterUrl = excerpt.chapterUrl,
                        category = excerpt.category,
                        note = excerpt.note,
                        createdAt = excerpt.createdAt,
                    )
                    inserted++
                }
            }
        }
        return inserted
    }

    private fun mapExcerpt(
        id: Long,
        text: String,
        mangaTitle: String,
        chapterName: String,
        chapterNumber: Double,
        mangaId: Long?,
        chapterId: Long?,
        sourceId: Long,
        mangaUrl: String,
        chapterUrl: String,
        category: String,
        note: String,
        createdAt: Long,
    ): Excerpt = Excerpt(
        id = id,
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

/**
 * LIKE treats '\', '%' and '_' as special; escape user input so a search for e.g. "100%"
 * matches literally. The queries declare ESCAPE '\'.
 */
private fun String.escapeLike(): String {
    val out = StringBuilder(length)
    forEach { c ->
        if (c == '\\' || c == '%' || c == '_') out.append('\\')
        out.append(c)
    }
    return out.toString()
}
