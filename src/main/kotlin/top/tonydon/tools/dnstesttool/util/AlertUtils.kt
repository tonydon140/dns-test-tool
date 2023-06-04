package top.tonydon.tools.dnstesttool.util

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.stage.Modality
import javafx.stage.Window

/**
 * 提示框工具类
 */
object AlertUtils {
    fun confirmation(head: String, context: String, window: Window) {
        alter(head, context, window, AlertType.CONFIRMATION)
    }

    fun none(head: String, context: String, window: Window) {
        alter(head, context, window, AlertType.NONE)
    }

    fun error(head: String, context: String, window: Window) {
        alter(head, context, window, AlertType.ERROR)
    }

    fun information(head: String, context: String, window: Window) {
        alter(head, context, window, AlertType.INFORMATION)
    }

    fun warning(head: String, context: String, window: Window) {
        alter(head, context, window, AlertType.WARNING)
    }

    private fun alter(head: String, context: String, window: Window, type: AlertType) {
        Platform.runLater {
            val alert = Alert(type)
            alert.headerText = head
            alert.contentText = context
            alert.initModality(Modality.WINDOW_MODAL)
            alert.initOwner(window)
            alert.show()
        }
    }
}
