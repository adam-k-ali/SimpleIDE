package com.adamkali.simpleide.preferences

import java.nio.file.Path
import java.nio.file.Paths

/**
 * OS user-config locations for SimpleIDE (Application Support / APPDATA / XDG).
 */
object AppPaths {
    fun configDir(
        osName: String = System.getProperty("os.name") ?: "",
        userHome: String = System.getProperty("user.home") ?: "",
        env: (String) -> String? = { System.getenv(it) }
    ): Path {
        val os = osName.lowercase()
        return when {
            os.contains("mac") -> Paths.get(userHome, "Library", "Application Support", "SimpleIDE")
            os.contains("win") -> {
                val appData = env("APPDATA")
                if (!appData.isNullOrBlank()) {
                    Paths.get(appData, "SimpleIDE")
                } else {
                    Paths.get(userHome, "AppData", "Roaming", "SimpleIDE")
                }
            }
            else -> {
                val xdg = env("XDG_CONFIG_HOME")
                if (!xdg.isNullOrBlank()) {
                    Paths.get(xdg, "simpleide")
                } else {
                    Paths.get(userHome, ".config", "simpleide")
                }
            }
        }
    }

    fun recentProjectsFile(
        osName: String = System.getProperty("os.name") ?: "",
        userHome: String = System.getProperty("user.home") ?: "",
        env: (String) -> String? = { System.getenv(it) }
    ): Path = configDir(osName, userHome, env).resolve("recent-projects.json")
}
