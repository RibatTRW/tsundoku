package eu.kanade.presentation.more.excerpts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.domain.excerpt.model.Excerpt
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.i18n.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExcerptVaultScreenContent(
    state: ExcerptVaultScreenState.Success,
    paddingValues: PaddingValues,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onOpenExcerpt: (Excerpt) -> Unit,
    onDeleteExcerpt: (Excerpt) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(MR.strings.excerpt_vault_search_hint)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
        )
        if (state.categories.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text(stringResource(MR.strings.excerpt_vault_all_categories)) },
                )
                state.categories.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category) },
                    )
                }
            }
        }
        val visible = state.visibleExcerpts
        if (visible.isEmpty()) {
            Text(
                text = stringResource(MR.strings.excerpt_vault_empty),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            ScrollbarLazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(visible, key = { it.id }) { excerpt ->
                    ExcerptCard(
                        excerpt = excerpt,
                        onOpen = { onOpenExcerpt(excerpt) },
                        onDelete = { onDeleteExcerpt(excerpt) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExcerptCard(
    excerpt: Excerpt,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by rememberSaveable(excerpt.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = excerpt.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${excerpt.mangaTitle} • ${excerpt.chapterName}",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (excerpt.category.isNotBlank() || excerpt.note.isNotBlank()) {
                Text(
                    text = listOf(excerpt.category, excerpt.note)
                        .filter { it.isNotBlank() }
                        .joinToString(" — "),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = excerptDateFormat.format(Date(excerpt.createdAt)),
                style = MaterialTheme.typography.labelSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onOpen) {
                    Text(stringResource(MR.strings.excerpt_vault_open_chapter))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(MR.strings.action_delete),
                    )
                }
            }
        }
    }
}

private val excerptDateFormat = SimpleDateFormat.getDateTimeInstance(
    SimpleDateFormat.MEDIUM,
    SimpleDateFormat.SHORT,
    Locale.getDefault(),
)
