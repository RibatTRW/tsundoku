package tachiyomi.data.excerpt

import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.flow.Flow
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList
import tachiyomi.domain.excerpt.model.Excerpt
import tachiyomi.domain.excerpt.repository.ExcerptRepository

class ExcerptRepositoryImpl(
    private val database: Database,
) : ExcerptRepository {

    override fun getExcerptsAsFlow(): Flow<List<Excerpt>> {
        return database.excerptsQueries.getExcerpts(::Excerpt).subscribeToList()
    }

    override suspend fun getAll(): List<Excerpt> {
        return database.excerptsQueries.getExcerpts(::Excerpt).awaitAsList()
    }

    override suspend fun insert(excerpts: List<Excerpt>) {
        database.transaction {
            excerpts.forEach {
                database.excerptsQueries.insert(
                    text = it.text,
                    mangaTitle = it.mangaTitle,
                    chapterName = it.chapterName,
                    chapterNumber = it.chapterNumber,
                    sourceId = it.sourceId,
                    mangaUrl = it.mangaUrl,
                    chapterUrl = it.chapterUrl,
                    createdAt = it.createdAt,
                )
            }
        }
    }

    override suspend fun delete(excerptId: Long) {
        database.excerptsQueries.delete(excerptId)
    }
}
