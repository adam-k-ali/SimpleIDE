package com.adamkali.simpleide.window

import java.awt.Component
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * File-dialog helpers for opening a `.proj` file.
 *
 * Swing's [JFileChooser] plus [FileNameExtensionFilter] is unreliable on macOS:
 * the Aqua/native dialog can report `CANCEL_OPTION` (or a path with the
 * extension stripped) after the user selects a file, which left Open Project
 * on the Home Screen with no error. AWT [FileDialog] uses NSOpenPanel and
 * returns the selection correctly.
 */
internal object ProjectFileChooser {
    fun isMacOS(osName: String = System.getProperty("os.name", "")): Boolean {
        return osName.lowercase().contains("mac")
    }

    /**
     * Maps a file-dialog selection to a `.proj` file.
     *
     * Handles picking the project folder, a `.proj` whose extension macOS hid,
     * or a path the chooser returned without `.proj`.
     */
    fun resolveProjectFile(selected: Path): Path? {
        if (Files.isRegularFile(selected)) {
            return selected.takeIf { hasProjExtension(it) }
        }

        val withExtension = selected.resolveSibling(selected.fileName.toString() + ".proj")
        if (Files.isRegularFile(withExtension)) {
            return withExtension
        }

        if (Files.isDirectory(selected)) {
            val named = selected.resolve("${selected.fileName}.proj")
            if (Files.isRegularFile(named)) {
                return named
            }
            val matches = projFilesIn(selected)
            return matches.singleOrNull()
        }

        return null
    }

    fun chooseProjectFile(parent: Component): Path? {
        return if (isMacOS()) {
            chooseFileWithAwtDialog(parent, "Open Project")
        } else {
            chooseFileWithSwing(parent)
        }
    }

    fun chooseDirectory(parent: Component): Path? {
        return if (isMacOS()) {
            chooseDirectoryWithAwtDialog(parent, "Choose Project Location")
        } else {
            chooseDirectoryWithSwing(parent)
        }
    }

    private fun chooseFileWithSwing(parent: Component): Path? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Open Project"
        chooser.fileFilter = FileNameExtensionFilter("SimpleIDE Project (*.proj)", "proj")
        chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        val result = chooser.showOpenDialog(dialogParent(parent))
        if (result != JFileChooser.APPROVE_OPTION) {
            return null
        }
        return chooser.selectedFile?.toPath()
    }

    private fun chooseDirectoryWithSwing(parent: Component): Path? {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Choose Project Location"
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val result = chooser.showDialog(dialogParent(parent), "Select")
        if (result != JFileChooser.APPROVE_OPTION) {
            return null
        }
        return chooser.selectedFile?.toPath()
    }

    private fun chooseFileWithAwtDialog(parent: Component, title: String): Path? {
        val dialog = FileDialog(frameOf(parent), title, FileDialog.LOAD)
        // Do not set FilenameFilter: macOS maps it to UTIs, and "proj" is not a
        // registered type, which can disable Open after the user selects a file.
        dialog.isVisible = true
        return selectedPath(dialog)
    }

    private fun chooseDirectoryWithAwtDialog(parent: Component, title: String): Path? {
        val previous = System.getProperty(MAC_FILE_DIALOG_DIRECTORIES)
        System.setProperty(MAC_FILE_DIALOG_DIRECTORIES, "true")
        try {
            val dialog = FileDialog(frameOf(parent), title, FileDialog.LOAD)
            dialog.isVisible = true
            return selectedPath(dialog)
        } finally {
            if (previous == null) {
                System.clearProperty(MAC_FILE_DIALOG_DIRECTORIES)
            } else {
                System.setProperty(MAC_FILE_DIALOG_DIRECTORIES, previous)
            }
        }
    }

    private fun selectedPath(dialog: FileDialog): Path? {
        val directory = dialog.directory ?: return null
        val file = dialog.file ?: return null
        return File(directory, file).toPath()
    }

    private fun dialogParent(parent: Component): Component {
        return SwingUtilities.getWindowAncestor(parent) ?: parent
    }

    private fun frameOf(parent: Component): Frame? {
        return SwingUtilities.getWindowAncestor(parent) as? Frame
    }

    private fun hasProjExtension(path: Path): Boolean {
        return path.fileName.toString().endsWith(".proj", ignoreCase = true)
    }

    private fun projFilesIn(directory: Path): List<Path> {
        if (!Files.isDirectory(directory)) {
            return emptyList()
        }
        val matches = mutableListOf<Path>()
        Files.newDirectoryStream(directory).use { stream ->
            for (path in stream) {
                if (Files.isRegularFile(path) && hasProjExtension(path)) {
                    matches.add(path)
                }
            }
        }
        return matches
    }

    private const val MAC_FILE_DIALOG_DIRECTORIES = "apple.awt.fileDialogForDirectories"
}
