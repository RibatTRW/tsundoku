package eu.kanade.tachiyomi.data.backup.restore.restorers

import eu.kanade.tachiyomi.data.backup.models.BackupExcerpt
import tachiyomi.domain.excerpt.repository.ExcerptRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ExcerptsRestorer(
    private val excerptRepository: ExcerptRepository = Injekt.get(),
) {

    /**
     * Restores excerpts, skipping entries already present so restoring the same
     * backup twice does not duplicate them. Returns how many were inserted.
     */
    suspend operator fun invoke(backupExcerpts: List<BackupExcerpt>): Int {
        if (backupExcerpts.isEmpty()) return 0
        return excerptRepository.insertMissing(backupExcerpts.map { it.toExcerpt() })
    }
}
