package tachiyomi.domain.excerpt.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.excerpt.model.Excerpt

interface ExcerptRepository {

    fun getExcerptsAsFlow(): Flow<List<Excerpt>>

    fun searchExcerptsAsFlow(query: String): Flow<List<Excerpt>>

    fun getCategoriesAsFlow(): Flow<List<String>>

    suspend fun getExcerpt(id: Long): Excerpt?

    suspend fun getAll(): List<Excerpt>

    suspend fun insert(excerpt: Excerpt): Long

    suspend fun delete(excerptId: Long)

    /**
     * Inserts the excerpts that are not already stored (matched on text, novel, chapter and
     * timestamp) and returns how many were inserted. Used by backup restore so restoring
     * the same backup twice does not duplicate entries.
     */
    suspend fun insertMissing(excerpts: List<Excerpt>): Int
}
