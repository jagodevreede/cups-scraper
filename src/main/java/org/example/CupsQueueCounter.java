package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class CupsQueueCounter {
    private static final CupsQueueParser parser = new CupsQueueParser();
    private static final String BEGIN_JSON = "{ \"count\": ";
    private static final String END_JSON = " }";

    static int port = 8080;
    static String url = "https://printer.local:631/jobs/";

    public static void main(String[] args) throws Exception {
        handleArguments(args);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/counter", new CounterHandler());
        server.createContext("/", new RootHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server started on port " + port + " scraping " + url + " open http://localhost:" + port + "/counter");
    }

    private static void handleArguments(String[] args) {
        if (args.length % 2 == 1 || args.length > 4) {
            System.err.println("Usage: CupsQueueCounter --port <port> --url https://printer.local:631/jobs/");
            System.exit(1);
        }
        for (int i = 0; i < args.length / 2; i++) {
            if (args[i].equals("--port")) {
                port = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("--url")) {
                url = args[i + 1];
            } else {
                System.err.println("Unknown option: " + args[i]);
                System.err.println("Usage: CupsQueueCounter --port <port> --url <url>");
                System.exit(1);
            }
        }
    }

    static class CounterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                    return;
                }

                String jobsHtml = parser.fetchCupsJobsHtml(url);
                var count = parser.countJobsInQueue(jobsHtml);
                String response = BEGIN_JSON + Long.toString(count) + END_JSON;

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] bytes = response.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        }
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<html><body><h1>Simple CUPS scraper for the length of the queue</h1>" +
                    "<p>Visit <a href=\"/counter\">/counter</a> to see the current number of items in the queue.</p></body></html>";
            byte[] bytes = html.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (exchange; OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
