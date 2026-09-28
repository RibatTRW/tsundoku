package tachiyomi.domain.excerpt.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.excerpt.model.Excerpt

interface ExcerptRepository {

    fun getExcerptsAsFlow(): Flow<List<Excerpt>>

    suspend fun getAll(): List<Excerpt>

    /** Inserts the excerpts, skipping any already stored (same text and timestamp). */
    suspend fun insert(excerpts: List<Excerpt>)

    suspend fun delete(excerptId: Long)
}
