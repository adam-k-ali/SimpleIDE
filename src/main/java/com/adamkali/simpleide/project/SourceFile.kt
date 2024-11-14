package com.adamkali.simpleide.project

import com.adamkali.simpleide.project.SourceFile.FileType
import java.nio.file.Path

/**
 * Represents a source file in a project.
 * @param path The path to the file.
 */
class SourceFile(private var path: Path) {

    /** The type of the file's contents.  */
    private var fileType = FileType.UNKNOWN
        get() = field
        set(value) {
            field = value
        }

    /**
     * Gets the name of the file.
     * @return The name of the file.
     */
    fun getFileName(): String {
        return path.fileName.toString()
    }

    /**
     * The possible type of the file's contents.
     * @return The type of the file's contents.
     */
    enum class FileType {
        UNKNOWN, CLASS, INTERFACE, ENUM, ANNOTATION, RECORD
    }
}