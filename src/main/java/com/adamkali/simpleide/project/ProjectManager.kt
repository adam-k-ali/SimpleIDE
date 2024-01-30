package com.adamkali.simpleide.project

import com.adamkali.simpleide.window.AppWindow
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Manages the active project
 * Also handles loading and saving projects
 */
object ProjectManager {
    /** The active project */
    var activeProject: Project? = null
        private set(value) {
            field = value
            AppWindow.setTitle(value?.getProjectName() ?: "SimpleIDE")
        }

    /** The GSON instance used to serialize and deserialize projects */
    private var gson = Gson()


    /**
     * Loads a package from the given path
     * @param rootPath The root path of the project
     * @param packageName The name of the package to load
     * @param isSourceFolder Whether the package is a source folder
     * @return The loaded package
     */
    private fun loadPackage(rootPath: String, packageName: String, isSourceFolder: Boolean = false): SourcePackage {
        val packagePath = Paths.get("$rootPath/$packageName")
        val sourcePackage = SourcePackage(packagePath, isSourceFolder)

        // Scan for files and sub-packages
        val packageFile = packagePath.toFile()
        val packageContents = packageFile.listFiles()
        if (packageContents == null) {
            println("Error: Failed to load package $packageName")
            return sourcePackage
        }

        for (file in packageContents) {
            if (file.isDirectory) {
                sourcePackage.addSourcePackage(loadPackage(rootPath, "$packageName/${file.name}"))
            } else {
                sourcePackage.addSourceFile(SourceFile(file.toPath()))
            }
        }
        return sourcePackage
    }

    /**
     * Loads a project from the given path
     * @param projectPath The path to the project file
     */
    fun load(projectPath: Path) {
        val reader = JsonReader(File(projectPath.toString()).reader())
        val projectFile: ProjectFile = gson.fromJson(reader, ProjectFile::class.java)
        projectFile.rootPath = projectPath.parent.toString()

        val project = Project(projectFile)

        // Load source folders
        for (sourceFolder in projectFile.sourcePaths) {
            project.addSourceFolder(loadPackage(projectFile.rootPath, sourceFolder, true))
        }

        activeProject = project

    }
}