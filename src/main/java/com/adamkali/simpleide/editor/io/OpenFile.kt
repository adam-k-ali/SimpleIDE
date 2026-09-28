package com.adamkali.simpleide.editor.io

import com.adamkali.simpleide.Global
import com.adamkali.simpleide.project.ProjectManager
import com.adamkali.simpleide.window.AppWindow
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JOptionPane

enum class UnsavedChoice {
    SAVE,
    DISCARD,
    CANCEL,
}

/**
 * Single open-file buffer: click-to-open, save, and reload from disk.
 */
object OpenFile {
    var path: Path? = null
        private set

    private var snapshot: String = ""

    var prompt: (fileName: String) -> UnsavedChoice = ::swingPrompt

    var showError: (title: String, message: String) -> Unit = ::swingError

    var onStateChanged: (() -> Unit)? = null

    fun isDirty(): Boolean {
        if (path == null) {
            return false
        }
        return Global.getCursor().getDocument().toText() != snapshot
    }

    fun reset() {
        path = null
        snapshot = ""
        prompt = ::swingPrompt
        showError = ::swingError
        onStateChanged = null
        notifyStateChanged()
    }

    /**
     * Loads [target] from disk into the current editor cursor.
     * Prompts when the current buffer has unsaved changes.
     * @return true if the file was loaded
     */
    fun open(target: Path): Boolean {
        if (!confirmIfDirty()) {
            return false
        }
        return loadFromDisk(target)
    }

    /**
     * Writes the current buffer to the active path.
     * @return true if the file was written
     */
    fun save(): Boolean {
        val target = path ?: return false
        return writeToDisk(target)
    }

    /**
     * Reloads the active path from disk.
     * Prompts when the current buffer has unsaved changes.
     * @return true if the file was reloaded
     */
    fun reload(): Boolean {
        val target = path ?: return false
        if (!confirmIfDirty()) {
            return false
        }
        return loadFromDisk(target)
    }

    /**
     * Prompts when the current buffer has unsaved changes.
     * @return true if the caller may proceed (clean, saved, or discarded)
     */
    fun confirmIfDirty(): Boolean {
        if (!isDirty()) {
            return true
        }
        val name = path?.fileName?.toString() ?: "Untitled"
        return when (prompt(name)) {
            UnsavedChoice.SAVE -> save()
            UnsavedChoice.DISCARD -> true
            UnsavedChoice.CANCEL -> false
        }
    }

    private fun loadFromDisk(target: Path): Boolean {
        val text = try {
            Files.readString(target, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            showError("Error", "Could not read ${target.fileName}: ${e.message}")
            return false
        }
        val cursor = Global.getCursor()
        cursor.getDocument().replaceText(text)
        cursor.clearSelection()
        cursor.moveTo(0, 0)
        path = target
        snapshot = cursor.getDocument().toText()
        updateTitle()
        return true
    }

    private fun writeToDisk(target: Path): Boolean {
        val text = Global.getCursor().getDocument().toText()
        try {
            Files.writeString(target, text, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            showError("Error", "Could not write ${target.fileName}: ${e.message}")
            return false
        }
        snapshot = text
        updateTitle()
        return true
    }

    private fun updateTitle() {
        val project = ProjectManager.activeProject?.getProjectName()
        val file = path?.fileName?.toString()
        val title = when {
            project != null && file != null -> "$project - $file"
            file != null -> file
            project != null -> project
            else -> "SimpleIDE"
        }
        AppWindow.setTitle(title)
        notifyStateChanged()
    }

    private fun notifyStateChanged() {
        onStateChanged?.invoke()
    }

    private fun swingPrompt(fileName: String): UnsavedChoice {
        val options = arrayOf("Save", "Discard", "Cancel")
        val result = JOptionPane.showOptionDialog(
            null,
            "$fileName has unsaved changes. Save before continuing?",
            "Unsaved Changes",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]
        )
        return when (result) {
            0 -> UnsavedChoice.SAVE
            1 -> UnsavedChoice.DISCARD
            else -> UnsavedChoice.CANCEL
        }
    }

    private fun swingError(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
    }
}
