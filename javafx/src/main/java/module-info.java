module top.tonydon.dns {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.sql;

    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;


    opens top.tonydon.dns to javafx.fxml;
    exports top.tonydon.dns;
    exports top.tonydon.dns.result;
}