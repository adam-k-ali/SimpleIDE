package com.adamkali.simpleide.preferences

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class RecentProjectsTest {
    @BeforeEach
    fun setUp() {
        RecentProjects.useTempStore()
    }

    @Test
    fun list_missingFile_isEmpty() {
        assertTrue(RecentProjects.list().isEmpty())
    }

    @Test
    fun record_writesNameAndAbsolutePath() {
        val dir = Files.createTempDirectory("simpleide-recent-proj-")
        dir.toFile().deleteOnExit()

        RecentProjects.record(dir, "Demo")

        val recents = RecentProjects.list()
        assertEquals(1, recents.size)
        assertEquals("Demo", recents[0].name)
        assertEquals(dir.toAbsolutePath().normalize(), recents[0].path.toAbsolutePath().normalize())
        val json = Files.readString(RecentProjects.file, StandardCharsets.UTF_8)
        assertTrue(json.contains("\"name\""), json)
        assertTrue(json.contains("Demo"), json)
        assertTrue(json.contains("\"path\""), json)
        assertTrue(json.contains("projects"), json)
    }

    @Test
    fun record_movesExistingPathToFront() {
        val first = Files.createTempDirectory("simpleide-recent-a-")
        val second = Files.createTempDirectory("simpleide-recent-b-")
        first.toFile().deleteOnExit()
        second.toFile().deleteOnExit()

        RecentProjects.record(first, "First")
        RecentProjects.record(second, "Second")
        RecentProjects.record(first, "First")

        val recents = RecentProjects.list()
        assertEquals(listOf("First", "Second"), recents.map { it.name })
        assertEquals(first.toAbsolutePath().normalize(), recents[0].path.toAbsolutePath().normalize())
    }

    @Test
    fun record_capsAtTen() {
        val dirs = (1..12).map { i ->
            Files.createTempDirectory("simpleide-recent-$i-").also { it.toFile().deleteOnExit() }
        }
        dirs.forEachIndexed { index, dir -> RecentProjects.record(dir, "P$index") }

        val recents = RecentProjects.list()
        assertEquals(RecentProjects.MAX_ENTRIES, recents.size)
        assertEquals("P11", recents.first().name)
        assertEquals("P2", recents.last().name)
    }

    @Test
    fun list_corruptJson_isEmpty() {
        Files.createDirectories(RecentProjects.file.parent)
        Files.writeString(RecentProjects.file, "{ not json", StandardCharsets.UTF_8)
        assertTrue(RecentProjects.list().isEmpty())
    }

    @Test
    fun remove_dropsMatchingPath() {
        val keep = Files.createTempDirectory("simpleide-recent-keep-")
        val drop = Files.createTempDirectory("simpleide-recent-drop-")
        keep.toFile().deleteOnExit()
        drop.toFile().deleteOnExit()

        RecentProjects.record(keep, "Keep")
        RecentProjects.record(drop, "Drop")
        RecentProjects.remove(drop)

        val recents = RecentProjects.list()
        assertEquals(listOf("Keep"), recents.map { it.name })
    }

    @Test
    fun reset_clearsStore() {
        val dir = Files.createTempDirectory("simpleide-recent-reset-")
        dir.toFile().deleteOnExit()
        RecentProjects.record(dir, "Gone")
        RecentProjects.reset()
        assertTrue(RecentProjects.list().isEmpty())
    }

    @Test
    fun list_ignoresBlankPaths() {
        Files.createDirectories(RecentProjects.file.parent)
        Files.writeString(
            RecentProjects.file,
            """{"projects":[{"name":"Bad","path":"  "},{"name":"Ok","path":"/tmp/ok"}]}""",
            StandardCharsets.UTF_8
        )
        val recents = RecentProjects.list()
        assertEquals(1, recents.size)
        assertEquals("Ok", recents[0].name)
        assertEquals(Paths.get("/tmp/ok"), recents[0].path)
    }
}
