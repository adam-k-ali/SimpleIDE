package com.adamkali.simpleide.window

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ProjectFileChooserTest {
    @Test
    fun isMacOS_detectsMacOsXAndMacOS() {
        assertTrue(ProjectFileChooser.isMacOS("Mac OS X"))
        assertTrue(ProjectFileChooser.isMacOS("macOS"))
        assertTrue(ProjectFileChooser.isMacOS("Mac OS X 10.16"))
        assertFalse(ProjectFileChooser.isMacOS("Linux"))
        assertFalse(ProjectFileChooser.isMacOS("Windows 11"))
    }

    @Test
    fun resolve_returnsProjFileUnchanged() {
        val proj = writeProj(tempDir(), "Demo")
        assertEquals(proj, ProjectFileChooser.resolveProjectFile(proj))
    }

    @Test
    fun resolve_acceptsUppercaseExtension() {
        val dir = tempDir()
        val proj = dir.resolve("Demo.PROJ")
        Files.writeString(proj, projJson("Demo"), StandardCharsets.UTF_8)
        assertEquals(proj, ProjectFileChooser.resolveProjectFile(proj))
    }

    @Test
    fun resolve_appendsProjWhenChooserStripsExtension() {
        val dir = tempDir()
        val proj = writeProj(dir, "Hidden")
        val stripped = dir.resolve("Hidden")
        assertEquals(proj, ProjectFileChooser.resolveProjectFile(stripped))
    }

    @Test
    fun resolve_findsProjInsideSelectedProjectFolder() {
        val parent = tempDir()
        val projectDir = parent.resolve("FolderProj")
        Files.createDirectories(projectDir.resolve("src"))
        val proj = writeProj(projectDir, "FolderProj")
        assertEquals(proj, ProjectFileChooser.resolveProjectFile(projectDir))
    }

    @Test
    fun resolve_findsSoleProjWhenFolderNameDoesNotMatch() {
        val dir = tempDir()
        Files.createDirectories(dir.resolve("src"))
        val proj = dir.resolve("OtherName.proj")
        Files.writeString(proj, projJson("OtherName"), StandardCharsets.UTF_8)
        assertEquals(proj, ProjectFileChooser.resolveProjectFile(dir))
    }

    @Test
    fun resolve_returnsNullWhenFolderHasSeveralProjFiles() {
        val dir = tempDir()
        Files.writeString(dir.resolve("A.proj"), projJson("A"), StandardCharsets.UTF_8)
        Files.writeString(dir.resolve("B.proj"), projJson("B"), StandardCharsets.UTF_8)
        assertNull(ProjectFileChooser.resolveProjectFile(dir))
    }

    @Test
    fun resolve_returnsNullForNonProjectFile() {
        val dir = tempDir()
        val javaFile = dir.resolve("Main.java")
        Files.writeString(javaFile, "class Main {}", StandardCharsets.UTF_8)
        assertNull(ProjectFileChooser.resolveProjectFile(javaFile))
    }

    @Test
    fun resolve_returnsNullForMissingPath() {
        assertNull(ProjectFileChooser.resolveProjectFile(tempDir().resolve("missing")))
    }

    private fun tempDir(): Path = Files.createTempDirectory("simpleide-chooser-").also {
        it.toFile().deleteOnExit()
    }

    private fun writeProj(directory: Path, name: String): Path {
        Files.createDirectories(directory)
        val dest = directory.resolve("$name.proj")
        Files.writeString(dest, projJson(name), StandardCharsets.UTF_8)
        return dest
    }

    private fun projJson(name: String): String =
        """{"projectName":"$name","sourcePaths":["src"]}"""
}
