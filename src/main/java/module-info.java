module com.bestchat.sjf {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires org.apache.commons.csv;

    exports com.bestchat.sjf;
    exports com.bestchat.sjf.io;
    exports com.bestchat.sjf.model;
    exports com.bestchat.sjf.scheduling;
    exports com.bestchat.sjf.simulation;
    exports com.bestchat.sjf.ui;
}
