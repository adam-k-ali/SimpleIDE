package com.adamkali.simpleide.preferences

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class RecentProject(
    val name: String,
    val path: Path
)

/**
 * MRU list of opened projects, persisted as JSON under [AppPaths.configDir].
 * [file] is injectable so tests never write to the real user config directory.
 */
object RecentProjects {
    const val MAX_ENTRIES = 10

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    var file: Path = defaultFile()

    /**
     * Points [file] at a fresh temp JSON path so tests do not write to the user config dir.
     */
    fun useTempStore() {
        file = Files.createTempDirectory("simpleide-recents-").resolve("recent-projects.json")
    }

    fun reset() {
        try {
            Files.deleteIfExists(file)
        } catch (_: Exception) {
        }
    }

    fun list(): List<RecentProject> = read()

    fun record(projectDir: Path, name: String) {
        try {
            val normalized = projectDir.toAbsolutePath().normalize()
            val key = normalized.toString()
            val rest = read().filterNot { samePath(it.path, key) }
            write(listOf(RecentProject(name, normalized)) + rest)
        } catch (_: Exception) {
        }
    }

    fun remove(projectDir: Path) {
        try {
            val key = projectDir.toAbsolutePath().normalize().toString()
            write(read().filterNot { samePath(it.path, key) })
        } catch (_: Exception) {
        }
    }

    private fun defaultFile(): Path = AppPaths.recentProjectsFile()

    private fun samePath(path: Path, key: String): Boolean {
        return try {
            path.toAbsolutePath().normalize().toString() == key
        } catch (_: Exception) {
            path.toString() == key
        }
    }

    private fun read(): List<RecentProject> {
        return try {
            if (!Files.isRegularFile(file)) {
                return emptyList()
            }
            val parsed = gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), PersistedList::class.java)
                ?: return emptyList()
            parsed.projects.orEmpty().mapNotNull { entry ->
                val pathText = entry.path?.trim().orEmpty()
                if (pathText.isEmpty()) {
                    null
                } else {
                    val stored = Paths.get(pathText)
                    val displayName = entry.name?.trim().orEmpty().ifEmpty {
                        stored.fileName?.toString() ?: pathText
                    }
                    RecentProject(displayName, stored)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun write(projects: List<RecentProject>) {
        val capped = projects.take(MAX_ENTRIES)
        val parent = file.parent
        if (parent != null) {
            Files.createDirectories(parent)
        }
        val persisted = PersistedList(
            capped.map { PersistedEntry(it.name, it.path.toString()) }.toTypedArray()
        )
        Files.writeString(file, gson.toJson(persisted), StandardCharsets.UTF_8)
    }

    private class PersistedList(var projects: Array<PersistedEntry>? = emptyArray())

    private class PersistedEntry(var name: String? = null, var path: String? = null)
}
