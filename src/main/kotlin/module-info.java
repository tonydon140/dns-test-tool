module top.tonydon.tools.dnstesttool {
    requires javafx.controls;
    requires kotlin.stdlib;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;


//    opens top.tonydon.tools.dnstesttool to javafx.controls;
//    opens top.tonydon.tools.dnstesttool.domain to javafx.base;
    opens top.tonydon.tools.dns.domain to com.fasterxml.jackson.databind;

    exports top.tonydon.tools.dns;
    exports top.tonydon.tools.dns.domain;
}