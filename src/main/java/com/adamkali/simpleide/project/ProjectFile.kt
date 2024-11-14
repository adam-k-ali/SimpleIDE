package com.adamkali.simpleide.project

/**
 * ProjectFile is a data class that represents a project file.
 * @property rootPath The root path of the project.
 * @property projectName The name of the project.
 * @property sourcePaths The paths of the source folders of the project.
 */
data class ProjectFile(
        var rootPath: String,
        var projectName: String,
        var sourcePaths: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectFile

        if (rootPath != other.rootPath) return false
        if (projectName != other.projectName) return false
        if (!sourcePaths.contentEquals(other.sourcePaths)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rootPath.hashCode()
        result = 31 * result + projectName.hashCode()
        result = 31 * result + sourcePaths.contentHashCode()
        return result
    }
}
