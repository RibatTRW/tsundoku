package eu.kanade.tachiyomi.ui.excerptvault

import androidx.lifecycle.viewModelScope
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.more.excerpts.ExcerptVaultDialog
import eu.kanade.presentation.more.excerpts.ExcerptVaultScreenState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import logcat.LogPriority
import mihon.core.viewmodel.StateViewModel
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.interactor.GetChapter
import tachiyomi.domain.excerpt.model.Excerpt
import tachiyomi.domain.excerpt.repository.ExcerptRepository
import tachiyomi.domain.manga.interactor.GetMangaByUrlAndSourceId
import tachiyomi.i18n.MR
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ExcerptVaultViewModel(
    private val excerptRepository: ExcerptRepository = Injekt.get(),
    private val getChapter: GetChapter = Injekt.get(),
    private val getMangaByUrlAndSourceId: GetMangaByUrlAndSourceId = Injekt.get(),
) : StateViewModel<ExcerptVaultScreenState>(ExcerptVaultScreenState.Loading) {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val excerptsFlow = query.flatMapLatest { q ->
        if (q.isBlank()) {
            excerptRepository.getExcerptsAsFlow()
        } else {
            excerptRepository.searchExcerptsAsFlow(q)
        }
    }

    init {
        viewModelScope.launchIO {
            combine(
                excerptsFlow,
                excerptRepository.getCategoriesAsFlow(),
                query,
                selectedCategory,
            ) { excerpts, categories, q, category ->
                ExcerptVaultScreenState.Success(
                    query = q,
                    // Drop the filter when its category no longer exists.
                    selectedCategory = category?.takeIf { it in categories },
                    excerpts = excerpts,
                    categories = categories,
                    dialog = (mutableState.value as? ExcerptVaultScreenState.Success)?.dialog,
                )
            }
                .catch { e -> logcat(LogPriority.ERROR, e) { "Failed to load excerpts" } }
                .collectLatest { mutableState.update { _ -> it } }
        }
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setCategory(category: String?) {
        selectedCategory.value = category
    }

    fun showDeleteDialog(excerpt: Excerpt) {
        mutableState.update {
            (it as? ExcerptVaultScreenState.Success)?.copy(dialog = ExcerptVaultDialog.Delete(excerpt))
                ?: it
        }
    }

    fun dismissDialog() {
        mutableState.update {
            (it as? ExcerptVaultScreenState.Success)?.copy(dialog = null) ?: it
        }
    }

    fun deleteExcerpt(id: Long) {
        viewModelScope.launchIO {
            excerptRepository.delete(id)
        }
        dismissDialog()
    }

    /**
     * Opens the source chapter when it is still available: stored ids first, then a
     * lookup by novel/chapter url for novels that were removed from (or never added to)
     * the library.
     */
    fun openExcerpt(excerpt: Excerpt) {
        viewModelScope.launchIO {
            var mangaId = excerpt.mangaId
            var chapterId = excerpt.chapterId?.takeIf { id ->
                getChapter.await(id)?.mangaId == mangaId
            }
            if (chapterId == null && excerpt.mangaUrl.isNotBlank() && excerpt.sourceId > 0) {
                val manga = getMangaByUrlAndSourceId.await(excerpt.mangaUrl, excerpt.sourceId)
                mangaId = manga?.id
                chapterId = if (manga != null && excerpt.chapterUrl.isNotBlank()) {
                    getChapter.await(excerpt.chapterUrl, manga.id)?.id
                } else {
                    null
                }
            }
            if (mangaId != null && chapterId != null) {
                eventChannel.send(Event.OpenReader(mangaId, chapterId))
            } else {
                eventChannel.send(Event.LocalizedMessage(MR.strings.excerpt_vault_chapter_unavailable))
            }
        }
    }

    sealed interface Event {
        data class OpenReader(val mangaId: Long, val chapterId: Long) : Event
        data class LocalizedMessage(val stringRes: StringResource) : Event
    }
}
