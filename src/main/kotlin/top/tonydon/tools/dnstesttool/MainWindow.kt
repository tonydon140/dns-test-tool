package top.tonydon.tools.dnstesttool

import javafx.application.HostServices
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.tonydon.tools.dnstesttool.constant.ClientConstants
import top.tonydon.tools.dnstesttool.domain.Dns
import top.tonydon.tools.dnstesttool.domain.DnsListResult
import top.tonydon.tools.dnstesttool.domain.PingResult
import top.tonydon.tools.dnstesttool.domain.VersionResult
import top.tonydon.tools.dnstesttool.exception.HttpException
import top.tonydon.tools.dnstesttool.exception.ResultException
import top.tonydon.tools.dnstesttool.util.AlertUtils
import top.tonydon.tools.dnstesttool.util.JSONUtils
import top.tonydon.tools.dnstesttool.util.PingUtils
import java.net.URI
import java.net.http.*
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


val log: Logger = LoggerFactory.getLogger(MainWindow::class.java)

val CORE_POOL_SIZE = ClientConstants.CPU_CORE_COUNT * 5
val MAX_POOL_SIZE = ClientConstants.CPU_CORE_COUNT * 10

val HTTP_CLIENT: HttpClient = HttpClient.newHttpClient()
val DNS_LIST_REQ: HttpRequest = HttpRequest.newBuilder()
    .uri(URI.create(ClientConstants.DNS_API))
    .header("Content-Type", "application/json")
    .timeout(Duration.ofMinutes(5))
    .build()
val pool = ThreadPoolExecutor(
    CORE_POOL_SIZE,
    MAX_POOL_SIZE,
    60L, TimeUnit.SECONDS, LinkedBlockingQueue()
)

class MainWindow {
    private val infoLabel = Label()
    private val progress = ProgressIndicator()
    private val table = TableView(FXCollections.emptyObservableList<Dns>())
    private var stage: Stage = Stage()
    private var hostServices: HostServices? = null

    private fun setInfo(info: String, visible: Boolean = true) {
        Platform.runLater {
            infoLabel.text = info
            progress.isVisible = visible
        }
    }

    private fun clearInfo() {
        Platform.runLater {
            infoLabel.text = ""
            progress.isVisible = false
        }
    }

    fun initWindow(stage: Stage, hostServices: HostServices): Parent {
        this.stage = stage;
        this.hostServices = hostServices;

        val root = BorderPane()
        val topBox = VBox()

        // 菜单栏
        val menuBar = MenuBar()
        val menu = Menu("帮助")
        val updateItem = MenuItem("检查更新")
        val aboutItem = MenuItem("关于")
        menu.items.addAll(updateItem, aboutItem)
        menuBar.menus.add(menu)

        // 控制栏
        val hBox = HBox()
        VBox.setMargin(hBox, Insets(10.0, 0.0, 10.0, 0.0))
        hBox.alignment = Pos.CENTER
        hBox.spacing = 20.0
        val refreshDnsListBtn = Button("刷新DNS列表")
        val testDelayBtn = Button("测试延迟")
        hBox.children.addAll(refreshDnsListBtn, testDelayBtn)

        // 将菜单栏和控制栏添加到TopBox中
        topBox.children.addAll(menuBar, hBox)
        root.top = topBox

        // 表格栏
        val desCol = TableColumn<Dns, String>("描述")
        desCol.cellValueFactory = PropertyValueFactory("description")
        desCol.prefWidth = 120.0
        table.columns.add(desCol)

        val ipCol = TableColumn<Dns, String>("DNS地址")
        ipCol.isEditable = true
        ipCol.cellValueFactory = PropertyValueFactory("ip")
        ipCol.prefWidth = 100.0

        table.columns.add(ipCol)

        val delayCol = TableColumn<Dns, Int>("延迟（ms）")
        delayCol.cellValueFactory = PropertyValueFactory("delay")
        delayCol.prefWidth = 80.0
        table.columns.add(delayCol)

        val infoCol = TableColumn<Dns, String>("信息")
        infoCol.cellValueFactory = PropertyValueFactory("info")
        infoCol.prefWidth = 140.0

        table.columns.add(infoCol)
        root.center = table

        // 状态栏
        val infoBox = HBox()
        infoBox.alignment = Pos.CENTER_LEFT
        infoBox.spacing = 5.0
        infoBox.prefHeight = 20.0
        progress.prefWidth = 15.0
        progress.prefHeight = 15.0
        progress.isVisible = false
        infoBox.children.addAll(infoLabel, progress)
        root.bottom = infoBox


        // 刷新 DNS 列表
        refreshDnsListBtn.setOnAction {
            findDnsList()
        }

        // 测试延迟
        testDelayBtn.setOnAction {
            testDelay()
        }

        table.setRowFactory {
            val row = TableRow<Dns>()
            // 双击将ip写入剪切板
            row.setOnMouseClicked {
                if (it.clickCount == 2 && !row.isEmpty) {
                    val clipboard: Clipboard = Clipboard.getSystemClipboard()
                    val content = ClipboardContent()
                    content.putString(row.item.ip)
                    clipboard.setContent(content)
                    setInfo("已复制：${row.item.ip}", false)
                }
            }
            row
        }

        aboutItem.setOnAction {
            this.hostServices!!.showDocument(ClientConstants.ABOUT_URL);
        }

        updateItem.setOnAction {
            checkUpdate()
        }

        return root
    }

    private fun checkUpdate(){
        setInfo("检查更新")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(ClientConstants.LATEST_VERSION_API))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofMinutes(5))
            .build()

        HTTP_CLIENT
            .sendAsync<String>(request, HttpResponse.BodyHandlers.ofString())
            .thenApply<String> { response: HttpResponse<String?> ->
                clearInfo()
                // 如果状态码不是200，抛出Http异常
                if (response.statusCode() != 200) {
                    throw HttpException(response.statusCode())
                }
                response.body()
            }.thenAccept {
                // 解析 JSON
                val versionResult = JSONUtils.parse(it, VersionResult::class.java)
                // 如果 code 不是 200，抛出结果异常
                if (versionResult.code != 200) {
                    throw ResultException(versionResult.msg)
                }
                // 获取版本信息
                val version = versionResult.data
                if (version != null) {
                    // 有新版本
                    if (version.versionNumber > ClientConstants.VERSION_NUMBER) {
                        Platform.runLater {
                            val alert = Alert(Alert.AlertType.CONFIRMATION)
                            alert.headerText = "发现新版本" + version.version + "！是否前往下载？"
                            alert.contentText = version.description
                            alert.initModality(Modality.WINDOW_MODAL)
                            alert.initOwner(stage)
                            alert.showAndWait()
                                .filter { buttonType: ButtonType -> buttonType == ButtonType.OK }
                                .ifPresent {
                                    this.hostServices?.showDocument(ClientConstants.LATEST_URL)
                                }
                        }
                    } else {
                        AlertUtils.information("当前版本" + ClientConstants.VERSION + "已是最新版本！", "", stage)
                    }
                }
            }.exceptionally { throwable: Throwable ->
                // 捕获异常
                val ex = throwable.cause
                throwable.printStackTrace()
                log.warn("{} : {}", ex!!.javaClass, ex.message)

                // 鉴别异常类型
                if (ex is ResultException) {
                    AlertUtils.error("请求出错", ex.message!!, stage)
                } else if (ex is HttpConnectTimeoutException) {
                    checkUpdate()
                } else if (ex is HttpTimeoutException) {
                    AlertUtils.error("请求超时", "网络请求超时，请稍后再试。", stage)
                } else {
                    // HTTP 错误、连接错误、等等
                    AlertUtils.error("网络错误", "检查更新发生错误，请稍后再试。", stage)
                }
                null
            }
    }

    private fun testDelay() {
        val dnsList = table.items
        if (dnsList.size != 0) {
            setInfo("测试延迟");
            val startTime = System.currentTimeMillis();
            val countDownLatch = CountDownLatch(dnsList.size)
            // 向线程池中添加任务
            for (dns in dnsList) {
                // 测试主 IP 延迟
                pool.execute {
                    dns.ip?.let {
                        val result: PingResult = PingUtils.ping(it)
                        dns.delay = result.meanDelay
                        dns.info = "成功：${result.successCount}次，失败：${result.totalCount - result.successCount}次"
                        log.debug("ip = {}, delay = {}", dns.ip, dns.delay)
                        table.refresh()
                        countDownLatch.countDown();
                    }
                }
            }
            // 监听线程池任务结束
            pool.execute {
                try {
                    countDownLatch.await()
                    log.debug("total time = {}s", (System.currentTimeMillis() - startTime) / 1000.0)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                clearInfo()
            }
        } else {
            AlertUtils.warning("数据错误", "请先获取DNS数据", stage);
        }
    }

    fun findDnsList() {
        setInfo("获取数据");
        HTTP_CLIENT
            .sendAsync(DNS_LIST_REQ, HttpResponse.BodyHandlers.ofString())
            .thenApply<String> { obj: HttpResponse<String?> ->
                obj.body()
            }
            .thenAccept {
                val dnsListResult = JSONUtils.parse(it, DnsListResult::class.java)
                Platform.runLater {
                    table.items = FXCollections.observableList(dnsListResult.data)
                }
            }.exceptionally {
                it.printStackTrace()
                AlertUtils.error("网络错误", "获取数据发生错误，请稍后再试或联系管理员。", this.stage);
                null
            }
        clearInfo()
    }

    fun close() {
        pool.shutdownNow()
        try {
            log.info("thread pool shutdown = {}", pool.awaitTermination(2, TimeUnit.SECONDS))
        } catch (e: InterruptedException) {
            pool.shutdownNow()
        }
    }
}