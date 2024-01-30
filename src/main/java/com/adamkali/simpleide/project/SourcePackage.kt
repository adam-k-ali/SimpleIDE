package com.adamkali.simpleide.project

import java.nio.file.Path

/**
 * Manages the properties of a source package or folder.
 * @param path The path to the source package from the root of the project.
 * @param isRoot Whether the package is the source folder itself. Defaults to false.
 */
class SourcePackage(private var path: Path, private var isRoot: Boolean = false) {

    /** The source files in the package. */
    var sourceFiles: MutableList<SourceFile> = mutableListOf()
        private set

    /** The sub-packages in the package. */
    var sourcePackages: MutableList<SourcePackage> = mutableListOf()
        private set

    /**
     * Adds a source file to the package.
     * @param sourceFile The source file to add.
     */
    fun addSourceFile(sourceFile: SourceFile) {
        sourceFiles.add(sourceFile)
    }

    /**
     * Adds a sub-package to the package.
     * @param sourcePackage The sub-package to add.
     */
    fun addSourcePackage(sourcePackage: SourcePackage) {
        sourcePackages.add(sourcePackage)
    }

    /**
     * Gets the name of the package.
     */
    fun getName(): String {
        return path.fileName.toString();
    }
}