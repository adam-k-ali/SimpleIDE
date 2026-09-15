package com.adamkali.simpleide.project

import com.adamkali.simpleide.activity.ProjectActivityListener
import com.adamkali.simpleide.window.AppWindow
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
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
    private var gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /** The callbacks for project activity */
    private var callbacks = mutableListOf<ProjectActivityListener>();

    fun registerCallback(listener: ProjectActivityListener) {
        if (!callbacks.contains(listener)) {
            callbacks.add(listener);
        }
    }

    fun deregisterCallback(listener: ProjectActivityListener) {
        if (callbacks.contains(listener)) {
            callbacks.remove(listener);
        }
    }

    /**
     * Clears the active project and listeners. Intended for tests.
     */
    fun reset() {
        callbacks.clear()
        activeProject = null
    }

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
        val projectFile: ProjectFile = File(projectPath.toString()).reader().use { reader ->
            gson.fromJson(JsonReader(reader), ProjectFile::class.java)
        }
        projectFile.rootPath = projectPath.parent.toString()

        val project = Project(projectFile)

        // Load source folders
        for (sourceFolder in projectFile.sourcePaths) {
            project.addSourceFolder(loadPackage(projectFile.rootPath, sourceFolder, true))
        }

        activeProject = project
        for (callback in callbacks) {
            callback.onProjectLoad(project)
        }
    }

    /**
     * Writes [projectFile] to [dest] as JSON with only `projectName` and `sourcePaths`.
     */
    fun save(projectFile: ProjectFile, dest: Path) {
        val persisted = PersistedProjectFile(projectFile.projectName, projectFile.sourcePaths)
        Files.writeString(dest, gson.toJson(persisted), StandardCharsets.UTF_8)
    }

    /**
     * Creates a new project folder under [parentDir], writes a `.proj` file and an empty `src/`
     * directory, then loads it.
     * @return the path to the new `.proj` file
     */
    fun create(parentDir: Path, projectName: String): Path {
        val name = projectName.trim()
        validateProjectName(name)
        if (!Files.isDirectory(parentDir)) {
            throw IllegalArgumentException("Parent directory does not exist")
        }

        val projectDir = parentDir.resolve(name)
        if (Files.exists(projectDir)) {
            throw IllegalArgumentException("A folder already exists with that name")
        }

        Files.createDirectories(projectDir.resolve("src"))
        val dest = projectDir.resolve("$name.proj")
        val projectFile = ProjectFile(
            rootPath = projectDir.toString(),
            projectName = name,
            sourcePaths = arrayOf("src")
        )
        save(projectFile, dest)
        load(dest)
        return dest
    }

    private fun validateProjectName(name: String) {
        if (name.isEmpty()) {
            throw IllegalArgumentException("Project name cannot be blank")
        }
        if (name == "." || name == "..") {
            throw IllegalArgumentException("Project name is invalid")
        }
        if (name.contains('/') || name.contains('\\')) {
            throw IllegalArgumentException("Project name cannot contain path separators")
        }
    }

    private class PersistedProjectFile(
        val projectName: String,
        val sourcePaths: Array<String>
    )
}
