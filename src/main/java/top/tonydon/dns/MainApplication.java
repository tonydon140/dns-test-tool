package top.tonydon.dns;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import top.tonydon.dns.constant.ClientConstants;

import java.io.IOException;

public class MainApplication extends Application {
    DnsController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main.fxml"));
        Parent parent = fxmlLoader.load();

        Scene scene = new Scene(parent, 600, 400);
        stage.setTitle(ClientConstants.TITLE);
        stage.setScene(scene);
        stage.show();

        // 获取 Controller，初始化
        controller = fxmlLoader.getController();
        controller.initAfterShow(this);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (controller == null)
            return;
        controller.close();
    }
}
