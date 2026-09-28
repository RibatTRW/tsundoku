package eu.kanade.tachiyomi.ui.excerptvault

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.excerpts.ExcerptVaultDialog
import eu.kanade.presentation.more.excerpts.ExcerptVaultScreenContent
import eu.kanade.presentation.more.excerpts.ExcerptVaultScreenState
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.LoadingScreen

class ExcerptVaultScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = viewModel<ExcerptVaultViewModel>()

        val state by viewModel.state.collectAsState()

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.label_excerpt_vault),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            if (state is ExcerptVaultScreenState.Loading) {
                LoadingScreen()
                return@Scaffold
            }

            val successState = state as ExcerptVaultScreenState.Success
            ExcerptVaultScreenContent(
                state = successState,
                paddingValues = paddingValues,
                onQueryChange = viewModel::setQuery,
                onCategorySelect = viewModel::setCategory,
                onOpenExcerpt = viewModel::openExcerpt,
                onDeleteExcerpt = viewModel::showDeleteDialog,
            )

            when (val dialog = successState.dialog) {
                null -> {}
                is ExcerptVaultDialog.Delete -> {
                    AlertDialog(
                        onDismissRequest = viewModel::dismissDialog,
                        title = { Text(stringResource(MR.strings.excerpt_vault_delete_confirm)) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.deleteExcerpt(dialog.excerpt.id) }) {
                                Text(stringResource(MR.strings.action_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = viewModel::dismissDialog) {
                                Text(stringResource(MR.strings.action_cancel))
                            }
                        },
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ExcerptVaultViewModel.Event.OpenReader -> {
                        context.startActivity(
                            ReaderActivity.newIntent(context, event.mangaId, event.chapterId),
                        )
                    }
                    is ExcerptVaultViewModel.Event.LocalizedMessage -> {
                        context.toast(event.stringRes)
                    }
                }
            }
        }
    }
}
