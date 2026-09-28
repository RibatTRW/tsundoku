package eu.kanade.tachiyomi.ui.excerptvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.chapter.interactor.GetChapter
import tachiyomi.domain.excerpt.model.Excerpt
import tachiyomi.domain.excerpt.repository.ExcerptRepository
import tachiyomi.domain.manga.interactor.GetMangaByUrlAndSourceId
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ExcerptVaultViewModel(
    private val excerptRepository: ExcerptRepository = Injekt.get(),
    private val getMangaByUrlAndSourceId: GetMangaByUrlAndSourceId = Injekt.get(),
    private val getChapter: GetChapter = Injekt.get(),
) : ViewModel() {

    /** Newest first; null until the first load. */
    val excerpts: StateFlow<List<Excerpt>?> = excerptRepository.getExcerptsAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(excerpt: Excerpt) {
        viewModelScope.launchIO { excerptRepository.delete(excerpt.id) }
    }

    /** Returns the (mangaId, chapterId) to open, or null when the chapter is not in the database. */
    suspend fun findChapter(excerpt: Excerpt): Pair<Long, Long>? {
        val manga = getMangaByUrlAndSourceId.await(excerpt.mangaUrl, excerpt.sourceId) ?: return null
        val chapter = getChapter.await(excerpt.chapterUrl, manga.id) ?: return null
        return manga.id to chapter.id
    }
}
