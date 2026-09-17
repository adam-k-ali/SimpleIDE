package com.adamkali.simpleide.project

import com.adamkali.simpleide.activity.ProjectActivityListener
import com.adamkali.simpleide.window.AppWindow
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Manages the active project
 * Also handles loading and saving projects
 */
object ProjectManager {
    private const val SIMPLE_DIR = ".simple"

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
     * Loads a project from the given directory. The directory is the project root: it should
     * contain a `.simple` folder (created if missing) with a `.proj` file (written from the
     * folder name if none exists). Source paths are resolved relative to [projectDir], not
     * `.simple`.
     */
    fun load(projectDir: Path) {
        if (!Files.isDirectory(projectDir)) {
            throw IllegalArgumentException("Project path is not a directory")
        }

        val projPath = ensureProjFile(projectDir)
        val projectFile: ProjectFile = Files.newBufferedReader(projPath, StandardCharsets.UTF_8).use { reader ->
            gson.fromJson(JsonReader(reader), ProjectFile::class.java)
        }
        projectFile.rootPath = projectDir.toString()

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
     * Creates a new project folder under [parentDir], writes `.simple/{name}.proj` and an empty
     * `src/` directory, then loads it.
     * @return the path to the new project directory
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
        writeNewProj(projectDir, name)
        load(projectDir)
        return projectDir
    }

    /**
     * Ensures [projectDir] has a `.simple` directory and exactly one `.proj` file.
     * Creates missing metadata using the folder name; does not create `src/`.
     */
    private fun ensureProjFile(projectDir: Path): Path {
        val simpleDir = projectDir.resolve(SIMPLE_DIR)
        if (Files.exists(simpleDir) && !Files.isDirectory(simpleDir)) {
            throw IllegalArgumentException(".simple exists but is not a directory")
        }
        Files.createDirectories(simpleDir)

        val projFiles = findProjFiles(simpleDir)
        return when {
            projFiles.isEmpty() -> {
                val name = projectDir.fileName?.toString()
                    ?: throw IllegalArgumentException("Project name is invalid")
                validateProjectName(name)
                writeNewProj(projectDir, name)
            }
            projFiles.size == 1 -> projFiles[0]
            else -> throw IllegalArgumentException(
                "Expected exactly one .proj file in .simple, found ${projFiles.size}"
            )
        }
    }

    private fun writeNewProj(projectDir: Path, name: String): Path {
        val simpleDir = projectDir.resolve(SIMPLE_DIR)
        Files.createDirectories(simpleDir)
        val dest = simpleDir.resolve("$name.proj")
        val projectFile = ProjectFile(
            rootPath = projectDir.toString(),
            projectName = name,
            sourcePaths = arrayOf("src")
        )
        save(projectFile, dest)
        return dest
    }

    private fun findProjFiles(simpleDir: Path): List<Path> {
        Files.newDirectoryStream(simpleDir).use { entries ->
            return entries
                .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".proj") }
                .sortedBy { it.fileName.toString() }
        }
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
