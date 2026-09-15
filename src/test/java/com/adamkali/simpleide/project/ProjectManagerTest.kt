package com.adamkali.simpleide.project

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ProjectManagerTest {
    @BeforeEach
    fun setUp() {
        ProjectManager.reset()
    }

    @Test
    fun create_writesProjFileAndEmptySrcThenLoads() {
        val parent = tempDir()
        val dest = ProjectManager.create(parent, "Demo")

        assertEquals(parent.resolve("Demo").resolve("Demo.proj"), dest)
        assertTrue(Files.isRegularFile(dest))
        assertTrue(Files.isDirectory(parent.resolve("Demo").resolve("src")))
        assertEquals(0, Files.list(parent.resolve("Demo").resolve("src")).use { it.count() })

        val project = ProjectManager.activeProject
        assertEquals("Demo", project?.getProjectName())
        val folders = project?.sourceFolders ?: emptyList()
        assertEquals(1, folders.size)
        assertEquals("src", folders[0].getName())
        assertTrue(folders[0].sourceFiles.isEmpty())
        assertTrue(folders[0].sourcePackages.isEmpty())
    }

    @Test
    fun create_trimsProjectName() {
        val parent = tempDir()
        val dest = ProjectManager.create(parent, "  Trimmed  ")
        assertEquals(parent.resolve("Trimmed").resolve("Trimmed.proj"), dest)
        assertEquals("Trimmed", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun create_failsIfFolderAlreadyExists() {
        val parent = tempDir()
        ProjectManager.create(parent, "Demo")
        val error = assertThrows(IllegalArgumentException::class.java) {
            ProjectManager.create(parent, "Demo")
        }
        assertTrue(error.message!!.contains("already exists"))
    }

    @Test
    fun create_rejectsBlankAndPathSeparatorNames() {
        val parent = tempDir()
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.create(parent, "   ") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.create(parent, ".") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.create(parent, "..") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.create(parent, "foo/bar") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.create(parent, "foo\\bar") }
        assertEquals(null, ProjectManager.activeProject)
    }

    @Test
    fun save_writesNameAndSourcePathsWithoutRootPath() {
        val dest = Files.createTempFile("simpleide-", ".proj")
        dest.toFile().deleteOnExit()
        val projectFile = ProjectFile("/should/not/appear", "Saved", arrayOf("src"))
        ProjectManager.save(projectFile, dest)

        val json = Files.readString(dest, StandardCharsets.UTF_8)
        assertTrue(json.contains("\"projectName\""), json)
        assertTrue(json.contains("Saved"), json)
        assertTrue(json.contains("\"sourcePaths\""), json)
        assertTrue(json.contains("src"), json)
        assertFalse(json.contains("rootPath"), "saved JSON should not include rootPath, was: $json")
        assertFalse(json.contains("/should/not/appear"), json)
    }

    @Test
    fun load_ofCreatedProjectExposesSrcSourceFolder() {
        val dest = ProjectManager.create(tempDir(), "Loaded")
        ProjectManager.reset()
        assertEquals(null, ProjectManager.activeProject)

        ProjectManager.load(dest)
        val folders = ProjectManager.activeProject!!.sourceFolders
        assertEquals(listOf("src"), folders.map { it.getName() })
    }

    private fun tempDir(): Path = Files.createTempDirectory("simpleide-project-").also {
        it.toFile().deleteOnExit()
    }
}
