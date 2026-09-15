package com.adamkali.simpleide.editor.io

import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

/**
 * Editor clipboard. Always stores text in memory so copy/paste works in
 * headless tests. When a display is available, also syncs with the system
 * clipboard so the editor can exchange text with other apps.
 */
object EditorClipboard {
    private var buffer: String = ""

    @JvmStatic
    fun setText(text: String) {
        buffer = text
        if (GraphicsEnvironment.isHeadless()) {
            return
        }
        try {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        } catch (_: Exception) {
            // Keep the in-memory buffer; paste still works inside the editor.
        }
    }

    @JvmStatic
    fun getText(): String? {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                val contents = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
                if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return contents.getTransferData(DataFlavor.stringFlavor) as String
                }
            } catch (_: Exception) {
                // Fall through to the in-memory buffer.
            }
        }
        return buffer.ifEmpty { null }
    }

    /**
     * Clears the in-memory buffer only. Does not touch the OS clipboard.
     */
    @JvmStatic
    fun reset() {
        buffer = ""
    }
}
