package com.maximgalushka.classifier.twitter.service;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * @since 8/29/2014.
 */
public class MainServiceStart implements Container {

    public static final Logger log = Logger.getLogger(MainServiceStart.class);

    public void headers(Request request, Response response) {
        long time = System.currentTimeMillis();
        response.setValue("Content-Type", "application/json");
        response.setValue("Server", "Tweets Clustering Classifier");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        response.setValue("Access-Control-Allow-Origin", "*");
    }

    public void handle(Request request, Response response) {
        try {
            headers(request, response);
            PrintStream body = response.getPrintStream();
            body.println("{h:1}");
            body.close();
            log.debug("Response sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] list) throws Exception {
        Container container = new MainServiceStart();
        Server server = new ContainerServer(container);
        Connection connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(8090);
        connection.connect(address);
        log.debug("Server started");
    }
}
