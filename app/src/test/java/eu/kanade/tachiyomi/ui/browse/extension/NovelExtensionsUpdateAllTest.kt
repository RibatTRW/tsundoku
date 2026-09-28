package eu.kanade.tachiyomi.ui.browse.extension

import eu.kanade.tachiyomi.extension.model.Extension
import mihon.domain.extension.model.ExtensionStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NovelExtensionsUpdateAllTest {

    private fun installedJsPlugin(
        name: String = "Fake Novel",
        isInstalled: Boolean = true,
        hasUpdate: Boolean = true,
    ): Extension.JsPlugin = Extension.JsPlugin(
        name = name,
        pkgName = "app.tsundoku.jsplugin.${name.lowercase().replace(" ", "-")}",
        versionName = "1.0.1",
        versionCode = 101L,
        libVersion = 0.0,
        lang = "en",
        isNsfw = false,
        isNovel = true,
        sources = listOf(
            Extension.Available.Source(
                id = 1L,
                lang = "en",
                name = name,
                baseUrl = "https://example.com",
            ),
        ),
        iconUrl = "",
        repoUrl = "https://example.com/plugins.json",
        isInstalled = isInstalled,
        hasUpdate = hasUpdate,
    )

    private fun installedApk(hasUpdate: Boolean): Extension.Installed = Extension.Installed(
        name = "Some Novel",
        pkgName = "eu.kanade.tachiyomi.extension.en.somenovel",
        versionName = "1.0.1",
        versionCode = 2L,
        libVersion = 1.5,
        lang = "en",
        isNsfw = false,
        isNovel = true,
        pkgFactory = null,
        sources = emptyList(),
        icon = null,
        hasUpdate = hasUpdate,
        isObsolete = false,
        isShared = true,
        store = null,
    )

    private fun availableJsPlugin(): Extension.JsPlugin = installedJsPlugin(
        name = "Not installed",
        isInstalled = false,
        hasUpdate = false,
    )

    private fun availableApk(): Extension.Available = Extension.Available(
        name = "Available Novel",
        pkgName = "eu.kanade.tachiyomi.extension.en.availablenovel",
        versionName = "1.0.0",
        versionCode = 1L,
        libVersion = 1.5,
        lang = "en",
        isNsfw = false,
        isNovel = true,
        sources = emptyList(),
        apkUrl = "https://example.com/ext.apk",
        iconUrl = "",
        store = ExtensionStore(
            indexUrl = "https://example.com/index.json",
            name = "Test repo",
            badgeLabel = "TEST",
            signingKey = "",
            contact = ExtensionStore.Contact(website = "", discord = null),
            isLegacy = false,
            extensionListUrl = null,
        ),
    )

    private fun untrusted(): Extension.Untrusted = Extension.Untrusted(
        name = "Untrusted Novel",
        pkgName = "eu.kanade.tachiyomi.extension.en.untrusted",
        versionName = "1.0.0",
        versionCode = 1L,
        libVersion = 1.5,
        signatureHash = "abc",
        isNovel = true,
    )

    @Test
    fun `installed js plugin with update is an update-all target`() {
        assertTrue(installedJsPlugin(isInstalled = true, hasUpdate = true).isUpdateAllTarget())
    }

    @Test
    fun `installed js plugin without update is not an update-all target`() {
        assertFalse(installedJsPlugin(isInstalled = true, hasUpdate = false).isUpdateAllTarget())
    }

    @Test
    fun `not installed js plugin is not an update-all target`() {
        assertFalse(availableJsPlugin().isUpdateAllTarget())
    }

    @Test
    fun `installed apk extension with update is an update-all target`() {
        assertTrue(installedApk(hasUpdate = true).isUpdateAllTarget())
    }

    @Test
    fun `installed apk extension without update is not an update-all target`() {
        assertFalse(installedApk(hasUpdate = false).isUpdateAllTarget())
    }

    @Test
    fun `available and untrusted extensions are never update-all targets`() {
        assertFalse(availableApk().isUpdateAllTarget())
        assertFalse(untrusted().isUpdateAllTarget())
    }

    @Test
    fun `update all selects every pending novel update and nothing else`() {
        val displayed = listOf(
            installedJsPlugin(name = "Fake Novel A"),
            installedJsPlugin(name = "Fake Novel B"),
            installedJsPlugin(name = "Fake Novel C"),
            installedApk(hasUpdate = true),
            installedJsPlugin(name = "Up to date", hasUpdate = false),
            installedApk(hasUpdate = false),
            availableJsPlugin(),
            availableApk(),
            untrusted(),
        )

        val targets = displayed.filter { it.isUpdateAllTarget() }

        assertEquals(
            setOf("Fake Novel A", "Fake Novel B", "Fake Novel C", "Some Novel"),
            targets.map { it.name }.toSet(),
        )
    }
}
