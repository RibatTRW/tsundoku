package eu.kanade.tachiyomi.ui.excerptvault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.launch
import tachiyomi.domain.excerpt.model.Excerpt
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.plus

class ExcerptVaultScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val viewModel = viewModel<ExcerptVaultViewModel>()

        val excerpts by viewModel.excerpts.collectAsState()
        var query by rememberSaveable { mutableStateOf<String?>(null) }
        var toDelete by remember { mutableStateOf<Excerpt?>(null) }

        Scaffold(
            topBar = { scrollBehavior ->
                SearchToolbar(
                    titleContent = { AppBarTitle(stringResource(MR.strings.label_excerpt_vault)) },
                    searchQuery = query,
                    onChangeSearchQuery = { query = it },
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            val all = excerpts
            if (all == null) {
                LoadingScreen()
                return@Scaffold
            }
            val q = query.orEmpty().trim()
            val visible = all.filter {
                q.isEmpty() ||
                    it.text.contains(q, ignoreCase = true) ||
                    it.mangaTitle.contains(q, ignoreCase = true) ||
                    it.chapterName.contains(q, ignoreCase = true)
            }
            if (visible.isEmpty()) {
                EmptyScreen(
                    stringRes = if (q.isEmpty()) MR.strings.excerpt_vault_empty else MR.strings.no_results_found,
                    modifier = Modifier.padding(paddingValues),
                )
                return@Scaffold
            }
            ScrollbarLazyColumn(
                contentPadding = PaddingValues(16.dp).plus(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(visible, key = { it.id }) { excerpt ->
                    ExcerptCard(
                        excerpt = excerpt,
                        onOpen = {
                            scope.launch {
                                val ids = try {
                                    viewModel.findChapter(excerpt)
                                } catch (_: Exception) {
                                    null
                                }
                                if (ids == null) {
                                    context.toast(MR.strings.excerpt_vault_chapter_unavailable)
                                } else {
                                    context.startActivity(ReaderActivity.newIntent(context, ids.first, ids.second))
                                }
                            }
                        },
                        onDelete = { toDelete = excerpt },
                    )
                }
            }
        }

        toDelete?.let { excerpt ->
            AlertDialog(
                onDismissRequest = { toDelete = null },
                title = { Text(stringResource(MR.strings.excerpt_vault_delete_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.delete(excerpt)
                            toDelete = null
                        },
                    ) {
                        Text(stringResource(MR.strings.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { toDelete = null }) {
                        Text(stringResource(MR.strings.action_cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun ExcerptCard(
    excerpt: Excerpt,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = excerpt.text, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${excerpt.mangaTitle} • ${excerpt.chapterName}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onOpen) {
                    Text(stringResource(MR.strings.excerpt_vault_open_chapter))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(MR.strings.action_delete))
                }
            }
        }
    }
}
