package top.tonydon.tools.dns

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import top.tonydon.tools.dns.constant.ClientConstants


class MainApplication : Application() {
    private val window = MainWindow()

    override fun start(stage: Stage) {
        stage.title = ClientConstants.TITLE
        stage.scene = Scene(window.initWindow(stage, hostServices), 455.0, 500.0)
        stage.show()
        window.findDnsList()
    }

    override fun stop() {
        window.close()
        super.stop()
    }
}

fun main() {
    Application.launch(MainApplication::class.java)
}