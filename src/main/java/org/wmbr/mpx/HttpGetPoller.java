package org.wmbr.mpx;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpGetPoller {

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger LOGGER = Logger.getLogger(HttpGetPoller.class.getName());

    HttpRequest request;
    HttpClient client;
    HttpResponseHandler<String> handler;
    int maxRetries;
    int failures = 0;

    public HttpGetPoller(URI endpoint, HttpClient client, HttpResponseHandler<String> handler, int maxRetries) {
        this.request = HttpRequest.newBuilder()
                .uri(endpoint)
                .GET()
                .build();
        this.client = client;
        this.maxRetries = maxRetries;
        this.handler = handler;
    }

    public void pollOnce() throws IOException, InterruptedException {
        HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
        handler.handle(res);
    }

    public void startPolling(long period, TimeUnit timeUnit) {
        Runnable poller = () -> {
            try {
                pollOnce();
                failures = 0;
            } catch (IOException | InterruptedException e) {
                failures++;
                LOGGER.log(Level.WARNING, "polling attempt " + failures + " failed", e);
                if (failures >= maxRetries) {
                    LOGGER.log(Level.SEVERE, "exceeded max retries (" + maxRetries + "), giving up", e);
                    scheduler.shutdownNow();
                }
            }
        };

        scheduler.scheduleAtFixedRate(poller, 0, period, timeUnit);
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        LOGGER.log(Level.INFO, "poller shut down");
    }
}