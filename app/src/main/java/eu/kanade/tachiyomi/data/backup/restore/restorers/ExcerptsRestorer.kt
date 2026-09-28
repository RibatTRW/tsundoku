package eu.kanade.tachiyomi.data.backup.restore.restorers

import eu.kanade.tachiyomi.data.backup.models.BackupExcerpt
import tachiyomi.domain.excerpt.repository.ExcerptRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ExcerptsRestorer(
    private val excerptRepository: ExcerptRepository = Injekt.get(),
) {

    suspend operator fun invoke(backupExcerpts: List<BackupExcerpt>) {
        excerptRepository.insert(backupExcerpts.map { it.toExcerpt() })
    }
}
