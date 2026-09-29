package com.adamkali.simpleide.project

import com.adamkali.simpleide.preferences.RecentProjects
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
        RecentProjects.useTempStore()
    }

    @Test
    fun create_writesProjFileAndEmptySrcThenLoads() {
        val parent = tempDir()
        val dest = ProjectManager.create(parent, "Demo")

        assertEquals(parent.resolve("Demo"), dest)
        assertTrue(Files.isRegularFile(dest.resolve(".simple").resolve("Demo.proj")))
        assertTrue(Files.isDirectory(dest.resolve("src")))
        assertEquals(0, Files.list(dest.resolve("src")).use { it.count() })

        val project = ProjectManager.activeProject
        assertEquals("Demo", project?.getProjectName())
        val folders = project?.sourceFolders ?: emptyList()
        assertEquals(1, folders.size)
        assertEquals("src", folders[0].getName())
        assertTrue(folders[0].sourceFiles.isEmpty())
        assertTrue(folders[0].sourcePackages.isEmpty())
        val recents = RecentProjects.list()
        assertEquals(1, recents.size)
        assertEquals("Demo", recents[0].name)
        assertEquals(dest.toAbsolutePath().normalize(), recents[0].path.toAbsolutePath().normalize())
    }

    @Test
    fun create_trimsProjectName() {
        val parent = tempDir()
        val dest = ProjectManager.create(parent, "  Trimmed  ")
        assertEquals(parent.resolve("Trimmed"), dest)
        assertTrue(Files.isRegularFile(dest.resolve(".simple").resolve("Trimmed.proj")))
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
        val recents = RecentProjects.list()
        assertEquals("Loaded", recents.first().name)
        assertEquals(dest.toAbsolutePath().normalize(), recents.first().path.toAbsolutePath().normalize())
    }

    @Test
    fun load_ofBareFolder_createsSimpleAndProjWithoutSrc() {
        val dir = tempDir().resolve("Bare")
        Files.createDirectories(dir)

        ProjectManager.load(dir)

        assertTrue(Files.isRegularFile(dir.resolve(".simple").resolve("Bare.proj")))
        assertFalse(Files.exists(dir.resolve("src")))
        assertEquals("Bare", ProjectManager.activeProject?.getProjectName())
        val json = Files.readString(dir.resolve(".simple").resolve("Bare.proj"), StandardCharsets.UTF_8)
        assertTrue(json.contains("\"projectName\""), json)
        assertTrue(json.contains("Bare"), json)
        assertTrue(json.contains("\"sourcePaths\""), json)
        assertFalse(json.contains("rootPath"), json)
    }

    @Test
    fun load_ofSimpleWithNoProj_writesProj() {
        val dir = tempDir().resolve("EmptyMeta")
        Files.createDirectories(dir.resolve(".simple"))

        ProjectManager.load(dir)

        assertTrue(Files.isRegularFile(dir.resolve(".simple").resolve("EmptyMeta.proj")))
        assertEquals("EmptyMeta", ProjectManager.activeProject?.getProjectName())
        assertEquals(1, Files.list(dir.resolve(".simple")).use { it.count() })
    }

    @Test
    fun load_ofExistingProj_doesNotOverwrite() {
        val dest = ProjectManager.create(tempDir(), "Kept")
        val proj = dest.resolve(".simple").resolve("Kept.proj")
        val before = Files.readString(proj, StandardCharsets.UTF_8)
        ProjectManager.reset()

        ProjectManager.load(dest)

        assertEquals(before, Files.readString(proj, StandardCharsets.UTF_8))
        assertEquals("Kept", ProjectManager.activeProject?.getProjectName())
    }

    @Test
    fun load_ofMultipleProj_fails() {
        val dir = tempDir().resolve("Multi")
        val simple = dir.resolve(".simple")
        Files.createDirectories(simple)
        Files.writeString(simple.resolve("a.proj"), """{"projectName":"A","sourcePaths":["src"]}""")
        Files.writeString(simple.resolve("b.proj"), """{"projectName":"B","sourcePaths":["src"]}""")

        val error = assertThrows(IllegalArgumentException::class.java) {
            ProjectManager.load(dir)
        }
        assertTrue(error.message!!.contains("exactly one"), error.message)
        assertEquals(null, ProjectManager.activeProject)
        assertTrue(RecentProjects.list().isEmpty())
    }

    @Test
    fun load_ofNonDirectory_fails() {
        val file = Files.createTempFile("simpleide-not-a-project-", ".txt")
        file.toFile().deleteOnExit()

        val error = assertThrows(IllegalArgumentException::class.java) {
            ProjectManager.load(file)
        }
        assertTrue(error.message!!.contains("not a directory"), error.message)
        assertEquals(null, ProjectManager.activeProject)
        assertTrue(RecentProjects.list().isEmpty())
    }

    @Test
    fun createFile_writesEmptyFileAndAddsToSourceFolder() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val src = ProjectManager.activeProject!!.sourceFolders.single()
        val proj = dest.resolve(".simple").resolve("Demo.proj")
        val before = Files.readString(proj, StandardCharsets.UTF_8)

        val created = ProjectManager.createFile(src, "Hello.java")

        assertEquals(dest.resolve("src").resolve("Hello.java"), created)
        assertTrue(Files.isRegularFile(created))
        assertEquals("", Files.readString(created, StandardCharsets.UTF_8))
        assertEquals(listOf("Hello.java"), src.sourceFiles.map { it.getFileName() })
        assertEquals(before, Files.readString(proj, StandardCharsets.UTF_8))
    }

    @Test
    fun createFolder_makesDirectoryAndAddsToSourceFolder() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val src = ProjectManager.activeProject!!.sourceFolders.single()

        val created = ProjectManager.createFolder(src, "util")

        assertEquals(dest.resolve("src").resolve("util"), created)
        assertTrue(Files.isDirectory(created))
        assertEquals(listOf("util"), src.sourcePackages.map { it.getName() })
        assertTrue(src.sourcePackages.single().sourceFiles.isEmpty())
    }

    @Test
    fun createFile_trimsName() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val src = ProjectManager.activeProject!!.sourceFolders.single()

        val created = ProjectManager.createFile(src, "  Hello.java  ")

        assertEquals(dest.resolve("src").resolve("Hello.java"), created)
        assertEquals("Hello.java", src.sourceFiles.single().getFileName())
    }

    @Test
    fun createFile_failsIfNameAlreadyExists() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val src = ProjectManager.activeProject!!.sourceFolders.single()
        ProjectManager.createFile(src, "Hello.java")

        val error = assertThrows(IllegalArgumentException::class.java) {
            ProjectManager.createFile(src, "Hello.java")
        }
        assertTrue(error.message!!.contains("already exists"), error.message)
        assertEquals(1, src.sourceFiles.size)

        val folderError = assertThrows(IllegalArgumentException::class.java) {
            ProjectManager.createFolder(src, "Hello.java")
        }
        assertTrue(folderError.message!!.contains("already exists"), folderError.message)
        assertTrue(Files.isRegularFile(dest.resolve("src").resolve("Hello.java")))
        assertFalse(Files.isDirectory(dest.resolve("src").resolve("Hello.java")))
    }

    @Test
    fun createFile_rejectsBlankAndPathSeparatorNames() {
        val dest = ProjectManager.create(tempDir(), "Demo")
        val src = ProjectManager.activeProject!!.sourceFolders.single()

        assertThrows(IllegalArgumentException::class.java) { ProjectManager.createFile(src, "   ") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.createFile(src, ".") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.createFile(src, "..") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.createFile(src, "foo/bar") }
        assertThrows(IllegalArgumentException::class.java) { ProjectManager.createFolder(src, "foo\\bar") }
        assertTrue(src.sourceFiles.isEmpty())
        assertTrue(src.sourcePackages.isEmpty())
        assertEquals(0, Files.list(dest.resolve("src")).use { it.count() })
    }

    private fun tempDir(): Path = Files.createTempDirectory("simpleide-project-").also {
        it.toFile().deleteOnExit()
    }
}
