package eu.kanade.presentation.more.excerpts

import tachiyomi.domain.excerpt.model.Excerpt

sealed interface ExcerptVaultScreenState {
    data object Loading : ExcerptVaultScreenState

    data class Success(
        val query: String,
        val selectedCategory: String?,
        val excerpts: List<Excerpt>,
        val categories: List<String>,
        val dialog: ExcerptVaultDialog?,
    ) : ExcerptVaultScreenState {
        val visibleExcerpts: List<Excerpt>
            get() = if (selectedCategory == null) {
                excerpts
            } else {
                excerpts.filter { it.category == selectedCategory }
            }
    }
}

sealed interface ExcerptVaultDialog {
    data class Delete(val excerpt: Excerpt) : ExcerptVaultDialog
}
