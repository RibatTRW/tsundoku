package eu.kanade.tachiyomi.ui.reader

import androidx.lifecycle.SavedStateHandle
import eu.kanade.domain.base.BasePreferences
import eu.kanade.domain.manga.interactor.SetMangaViewerFlags
import eu.kanade.domain.source.interactor.GetIncognitoState
import tachiyomi.domain.source.service.SourceManager
import eu.kanade.domain.track.interactor.TrackChapter
import eu.kanade.domain.track.service.TrackPreferences
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.download.DownloadProvider
import eu.kanade.tachiyomi.data.saver.ImageSaver
import eu.kanade.tachiyomi.data.track.source.SourceTrackerDispatcher
import eu.kanade.tachiyomi.data.translation.TranslationService
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.interactor.GetChaptersByMangaId
import tachiyomi.domain.chapter.interactor.UpdateChapter
import tachiyomi.domain.download.service.DownloadPreferences
import tachiyomi.domain.excerpt.model.Excerpt
import tachiyomi.domain.excerpt.repository.ExcerptRepository
import tachiyomi.domain.history.interactor.GetNextChapters
import tachiyomi.domain.history.interactor.UpsertHistory
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.translation.service.TranslationPreferences

/**
 * Pins the save-excerpt result contract the reader UI relies on: a confirmed
 * insert emits Success (the only path that toasts "saved"), while a failing
 * insert emits Error and never reports success.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReaderSaveExcerptTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeExcerptRepository(
        var failWith: Throwable? = null,
    ) : ExcerptRepository {
        val inserted = mutableListOf<Excerpt>()

        override fun getExcerptsAsFlow(): Flow<List<Excerpt>> = emptyFlow()

        override fun searchExcerptsAsFlow(query: String): Flow<List<Excerpt>> = emptyFlow()

        override fun getCategoriesAsFlow(): Flow<List<String>> = emptyFlow()

        override suspend fun getExcerpt(id: Long): Excerpt? = null

        override suspend fun getAll(): List<Excerpt> = inserted.toList()

        override suspend fun insert(excerpt: Excerpt): Long {
            failWith?.let { throw it }
            inserted.add(excerpt)
            return inserted.size.toLong()
        }

        override suspend fun delete(excerptId: Long) {
            inserted.removeAll { it.id == excerptId }
        }

        override suspend fun insertMissing(excerpts: List<Excerpt>): Int = 0
    }

    private fun viewModel(repository: ExcerptRepository): ReaderViewModel {
        val translationPreferences: TranslationPreferences = mockk {
            every { translationEnabled().get() } returns false
            every { smartAutoTranslate().get() } returns false
        }
        val downloadPreferences: DownloadPreferences = mockk {
            every { autoDownloadWhileReading.get() } returns 0
        }
        val readerPreferences: ReaderPreferences = mockk {
            every { novelBottomBarItems.get() } returns "[]"
            every { novelBottomBarItems.changes() } returns emptyFlow()
        }
        return ReaderViewModel(
            savedState = SavedStateHandle(),
            sourceManager = mockk(relaxed = true),
            downloadManager = mockk(relaxed = true),
            downloadProvider = mockk(relaxed = true),
            imageSaver = mockk(relaxed = true),
            readerPreferences = readerPreferences,
            basePreferences = mockk(relaxed = true),
            downloadPreferences = downloadPreferences,
            trackPreferences = mockk(relaxed = true),
            trackChapter = mockk(relaxed = true),
            sourceTrackerDispatcher = mockk(relaxed = true),
            getManga = mockk(relaxed = true),
            getChaptersByMangaId = mockk(relaxed = true),
            getNextChapters = mockk(relaxed = true),
            upsertHistory = mockk(relaxed = true),
            updateChapter = mockk(relaxed = true),
            setMangaViewerFlags = mockk(relaxed = true),
            getIncognitoState = mockk(relaxed = true),
            libraryPreferences = mockk(relaxed = true),
            translationPreferences = translationPreferences,
            translationService = mockk(relaxed = true),
            getLibraryManga = mockk(relaxed = true),
            excerptRepository = repository,
        )
    }

    private fun draft() = ReaderViewModel.ExcerptDraft(
        text = "morning",
        mangaId = 42L,
        mangaTitle = "VaultTestNovel",
        sourceId = 3L,
        mangaUrl = "/novel",
        chapterId = 7L,
        chapterName = "Chapter 1 - The Beginning",
        chapterNumber = 1.0,
        chapterUrl = "/novel/ch1",
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `failing insert emits Error and never reports success`() = runTest {
        val repository = FakeExcerptRepository(failWith = IllegalStateException("database is locked"))
        val viewModel = viewModel(repository)
        viewModel.openSaveExcerptDialog(draft())

        val events = mutableListOf<ReaderViewModel.Event>()
        val collectJob = launch { viewModel.eventFlow.take(1).toList(events) }
        viewModel.saveExcerpt("wisdom", "")
        advanceUntilIdle()
        collectJob.join()

        assertEquals(1, events.size)
        val result = (events[0] as ReaderViewModel.Event.SaveExcerpt).result
        assertTrue(result is ReaderViewModel.SaveExcerptResult.Error)
        assertEquals("database is locked", (result as ReaderViewModel.SaveExcerptResult.Error).error.message)
        assertTrue(repository.inserted.isEmpty())
        assertNull(viewModel.state.value.dialog)
    }

    @Test
    fun `confirmed insert emits Success with the drafted excerpt`() = runTest {
        val repository = FakeExcerptRepository()
        val viewModel = viewModel(repository)
        viewModel.openSaveExcerptDialog(draft())

        val events = mutableListOf<ReaderViewModel.Event>()
        val collectJob = launch { viewModel.eventFlow.take(1).toList(events) }
        viewModel.saveExcerpt("wisdom", "opening")
        advanceUntilIdle()
        collectJob.join()

        assertEquals(1, events.size)
        assertTrue(
            (events[0] as ReaderViewModel.Event.SaveExcerpt).result is
                ReaderViewModel.SaveExcerptResult.Success,
        )
        assertEquals(1, repository.inserted.size)
        assertEquals("morning", repository.inserted[0].text)
        assertEquals("VaultTestNovel", repository.inserted[0].mangaTitle)
        assertEquals("Chapter 1 - The Beginning", repository.inserted[0].chapterName)
        assertEquals("wisdom", repository.inserted[0].category)
        assertEquals("opening", repository.inserted[0].note)
        assertNull(viewModel.state.value.dialog)
    }

    @Test
    fun `saveExcerpt without a dialog does nothing`() = runTest {
        val repository = FakeExcerptRepository()
        val viewModel = viewModel(repository)

        viewModel.saveExcerpt("wisdom", "")
        advanceUntilIdle()

        assertTrue(repository.inserted.isEmpty())
        // No event is emitted; the flow stays silent.
        var emitted = false
        val collectJob = launch { viewModel.eventFlow.first().let { emitted = true } }
        advanceUntilIdle()
        collectJob.cancel()
        assertTrue(!emitted)
    }
}
