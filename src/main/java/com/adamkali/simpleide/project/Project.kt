package com.adamkali.simpleide.project

/**
 * The Project class represents a project in the IDE.
 * It contains a list of source packages and folders.
 * @param projectFile The project file that contains the project name and path.
 */
class Project(private var projectFile: ProjectFile) {
    /** The list of source folders in the project. */
    var sourceFolders: MutableList<SourcePackage> = mutableListOf()
        private set

    /**
     * Add a source package to the project
     * @param sourceFolder The source package to add to the project.
     */
    fun addSourceFolder(sourceFolder: SourcePackage) {
        sourceFolders.add(sourceFolder)
    }

    /**
     * Get the project name.
     * @return The project name.
     */
    fun getProjectName(): String {
        return projectFile.projectName
    }
}