package top.tonydon.dns;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.tonydon.dns.constant.ClientConstants;
import top.tonydon.dns.exception.HttpException;
import top.tonydon.dns.exception.ResultException;
import top.tonydon.dns.result.Dns;
import top.tonydon.dns.result.DnsListResult;
import top.tonydon.dns.result.Version;
import top.tonydon.dns.result.VersionResult;
import top.tonydon.dns.util.AlertUtils;
import top.tonydon.dns.util.JSONUtils;
import top.tonydon.dns.util.PingUtils;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;

public class MainController {
    // 日志
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    // 线程池
    private final ExecutorService pool;
    private final int CORE_POOL_SIZE = ClientConstants.CPU_CORE_COUNT * 5;
    private final int MAX_POOL_SIZE = ClientConstants.CPU_CORE_COUNT * 10;

    // HTTP
    private final HttpClient HTTP_CLIENT;
    private final HttpRequest DNS_LIST_REQ;

    public TableView<Dns> tableView;
    public VBox root;
    public Label infoLabel;
    public ProgressIndicator infoProgress;

    private Stage primaryStage;
    private HostServices hostServices;

    public MainController() {

        this.HTTP_CLIENT = HttpClient.newHttpClient();

        this.DNS_LIST_REQ = HttpRequest.newBuilder()
                .uri(URI.create(ClientConstants.DNS_API))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(5))
                .build();

        this.pool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 组件加载后的初始化工作
     *
     * @param application Application 实例
     */
    public void initAfterShow(Application application) {
        this.primaryStage = (Stage) root.getScene().getWindow();
        this.hostServices = application.getHostServices();
        findDnsList();
    }

    /**
     * 测试延迟
     */
    public void testDelay() {
        setInfo("测试延迟");
        final long startTime = System.currentTimeMillis();

        ObservableList<Dns> dnsList = tableView.getItems();
        if (dnsList.size() == 0) {
            AlertUtils.warning("数据错误", "请先获取DNS数据", this.primaryStage);
        }
        CountDownLatch countDownLatch = new CountDownLatch(dnsList.size());

        // 向线程池中添加任务
        for (Dns dns : dnsList) {
            // 测试主 IP 延迟
            pool.execute(() -> {
                String delay = PingUtils.ping(dns.getPrimaryIp());
                dns.setPrimaryDelay(delay);
                log.debug("ip = {}, delay = {}", dns.getPrimaryIp(), delay);
                tableView.refresh();
            });
            // 测试副 IP 延迟
            pool.execute(() -> {
                String delay = PingUtils.ping(dns.getAssistantIp());
                dns.setAssistantDelay(delay);
                log.debug("ip = {}, delay = {}", dns.getAssistantIp(), delay);
                tableView.refresh();
                countDownLatch.countDown();
            });
        }


        // 监听线程池任务结束
        pool.execute(() -> {
            try {
                countDownLatch.await();
                log.debug("total time = {}s", (System.currentTimeMillis() - startTime) / 1000.0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            clearInfo();
        });
    }


    /**
     * 获取 DNS 列表
     */
    public void findDnsList() {
        setInfo("获取数据");
        this.HTTP_CLIENT
                .sendAsync(this.DNS_LIST_REQ, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    // 解析 json 数据
                    DnsListResult result = JSONUtils.parse(body, DnsListResult.class);
                    // 加载为可观察列表
                    ObservableList<Dns> observableList = FXCollections.observableList(result.getData());
                    tableView.setItems(observableList);
                    clearInfo();
                }).exceptionally(throwable -> {
                    throwable.printStackTrace();
                    AlertUtils.error("网络错误", "检查更新发生错误，请稍后再试。", this.primaryStage);
                    return null;
                });
    }

    private void clearInfo() {
        Platform.runLater(() -> {
            infoLabel.setText("");
            infoProgress.setVisible(false);
        });
    }

    private void setInfo(String info) {
        Platform.runLater(() -> {
            infoLabel.setText(info);
            infoProgress.setVisible(true);
        });
    }

    /**
     * 检查更新
     */
    public void checkUpdate() {
        setInfo("检查更新中");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ClientConstants.LATEST_VERSION_API))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(5))
                .build();

        this.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    clearInfo();
                    // 如果状态码不是200，抛出Http异常
                    if (response.statusCode() != 200) {
                        throw new HttpException(response.statusCode());
                    }
                    return response.body();
                }).thenAccept(body -> {
                    // 解析 JSON
                    VersionResult result = JSONUtils.parse(body, VersionResult.class);
                    Version version = result.getData();

                    // 如果 code 不是 200，抛出结果异常
                    if (result.getCode() != 200) {
                        throw new ResultException(result.getMsg());
                    }

                    // 有新版本
                    if (version.getVersionNumber() > ClientConstants.VERSION_NUMBER) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setHeaderText("发现新版本" + version.getVersion() + "！是否前往下载？");
                            alert.setContentText(version.getDescription());
                            alert.initModality(Modality.WINDOW_MODAL);
                            alert.initOwner(primaryStage);
                            alert.showAndWait()
                                    .filter(buttonType -> buttonType == ButtonType.OK)
                                    .ifPresent(response -> this.hostServices.showDocument(ClientConstants.LATEST_URL));
                        });
                    } else {
                        AlertUtils.information("当前版本" + ClientConstants.VERSION + "已是最新版本！", "", primaryStage);
                    }
                }).exceptionally(throwable -> {
                    // 捕获异常
                    Throwable ex = throwable.getCause();
                    throwable.printStackTrace();
                    log.warn("{} : {}", ex.getClass(), ex.getMessage());

                    // 鉴别异常类型
                    if (ex instanceof ResultException) {
                        AlertUtils.error("请求出错", ex.getMessage(), this.primaryStage);
                    } else if (ex instanceof HttpConnectTimeoutException) {
                        checkUpdate();
                    } else if (ex instanceof HttpTimeoutException) {
                        AlertUtils.error("请求超时", "网络请求超时，请稍后再试。", this.primaryStage);
                    } else {
                        // HTTP 错误、连接错误、等等
                        AlertUtils.error("网络错误", "检查更新发生错误，请稍后再试。", this.primaryStage);
                    }
                    return null;
                });
    }

    public void about() {
        this.hostServices.showDocument(ClientConstants.ABOUT_URL);
    }

    public void close() {
        // 关闭线程池
        this.pool.shutdownNow();
        try {
            log.info("thread pool shutdown = {}", this.pool.awaitTermination(2, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            this.pool.shutdownNow();
        }
    }
}