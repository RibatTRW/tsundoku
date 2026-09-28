package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.models.BackupExcerpt
import eu.kanade.tachiyomi.data.backup.models.backupExcerptMapper
import tachiyomi.domain.excerpt.repository.ExcerptRepository
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ExcerptsBackupCreator(
    private val excerptRepository: ExcerptRepository = Injekt.get(),
) {

    suspend operator fun invoke(): List<BackupExcerpt> {
        return excerptRepository.getAll().map(backupExcerptMapper)
    }
}
