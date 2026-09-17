package com.adamkali.simpleide.preferences

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class AppPathsTest {
    @Test
    fun configDir_macUsesApplicationSupport() {
        val dir = AppPaths.configDir("Mac OS X", "/Users/ada") { null }
        assertEquals(Paths.get("/Users/ada/Library/Application Support/SimpleIDE"), dir)
    }

    @Test
    fun configDir_windowsUsesAppData() {
        val dir = AppPaths.configDir("Windows 11", "C:\\Users\\ada") { key ->
            if (key == "APPDATA") "C:\\Users\\ada\\AppData\\Roaming" else null
        }
        assertEquals(Paths.get("C:\\Users\\ada\\AppData\\Roaming", "SimpleIDE"), dir)
    }

    @Test
    fun configDir_windowsFallsBackWithoutAppData() {
        val dir = AppPaths.configDir("Windows 10", "C:\\Users\\ada") { null }
        assertEquals(Paths.get("C:\\Users\\ada", "AppData", "Roaming", "SimpleIDE"), dir)
    }

    @Test
    fun configDir_linuxUsesXdgConfigHome() {
        val dir = AppPaths.configDir("Linux", "/home/ada") { key ->
            if (key == "XDG_CONFIG_HOME") "/custom/config" else null
        }
        assertEquals(Paths.get("/custom/config/simpleide"), dir)
    }

    @Test
    fun configDir_linuxFallsBackToDotConfig() {
        val dir = AppPaths.configDir("Linux", "/home/ada") { null }
        assertEquals(Paths.get("/home/ada/.config/simpleide"), dir)
    }

    @Test
    fun recentProjectsFile_isJsonUnderConfigDir() {
        val file = AppPaths.recentProjectsFile("Mac OS X", "/Users/ada") { null }
        assertEquals(
            Paths.get("/Users/ada/Library/Application Support/SimpleIDE/recent-projects.json"),
            file
        )
    }
}
