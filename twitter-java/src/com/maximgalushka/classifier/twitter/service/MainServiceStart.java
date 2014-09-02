package com.maximgalushka.classifier.twitter.service;

import com.google.gson.Gson;
import com.maximgalushka.classifier.twitter.clusters.Clusters;
import com.maximgalushka.classifier.twitter.stream.TwitterStreamProcessor;
import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * TODO: 1 thread server, design is exceptionally bad
 *
 * @since 8/29/2014.
 */
public class MainServiceStart implements Container, Updatable<Clusters> {

    public static final Logger log = Logger.getLogger(MainServiceStart.class);

    private static final Clusters EMPTY = new Clusters(false);
    private final Clusters model = new Clusters();
    private volatile boolean updated = false;
    private Gson gson;

    public MainServiceStart() {
        this.gson = new Gson();
    }

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
            synchronized (model) {
                if (!updated) {
                    body.println(gson.toJson(EMPTY));
                } else {
                    body.println(gson.toJson(model));
                    updated = false;
                }
            }
            body.close();
            log.debug("Response sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] list) throws Exception {
        MainServiceStart container = new MainServiceStart();
        Server server = new ContainerServer(container);
        Connection connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(8090);
        connection.connect(address);
        log.debug("Server started");

        // TODO: via executors
        new Thread(new TwitterStreamProcessor(container)).start();
        log.debug("Twitter stream processor started");
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public void update(Clusters model) {
        synchronized (model) {
            this.model.cleanClusters();
            this.model.addClusters(model.getClusters());
            this.updated = true;
        }
    }
}
