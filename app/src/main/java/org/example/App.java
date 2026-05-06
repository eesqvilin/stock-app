package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    private static final Queue<StockSnapshot> queue = new ConcurrentLinkedQueue<>();
    private static final String DOW_JONES_SYMBOL = "%5EDJI"; // URL-encoded ^DJI

    // Yahoo Finance v8 chart endpoint — no crumb/cookie required
    private static final String YF_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1m&range=1d";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public record StockSnapshot(Instant timestamp, BigDecimal price) {}

    public static class StockFetcher {

        public StockSnapshot fetch(String symbol) throws IOException, InterruptedException {
            String url = String.format(YF_URL, symbol);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    // Mimic a real browser — this is the key fix for 429s
                    .header("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                                    + "Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Referer", "https://finance.yahoo.com/")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new IOException("Rate limited (429). Back off and retry.");
            }
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP status: " + response.statusCode());
            }

            return parsePrice(response.body());
        }

        private StockSnapshot parsePrice(String json) throws IOException {
            JsonNode root = MAPPER.readTree(json);
            JsonNode meta = root.path("chart").path("result").get(0).path("meta");

            // "regularMarketPrice" is the most reliable field in the v8 meta block
            JsonNode priceNode = meta.path("regularMarketPrice");
            if (priceNode.isMissingNode()) {
                throw new IOException("Price field missing in response");
            }

            return new StockSnapshot(Instant.now(), priceNode.decimalValue());
        }
    }

    public static void main(String[] args) {
        StockFetcher fetcher = new StockFetcher();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                StockSnapshot snapshot = fetcher.fetch(DOW_JONES_SYMBOL);
                queue.add(snapshot);
                System.out.println("Added: " + snapshot);
            } catch (Exception e) {
                System.err.println("Fetch failed: " + e.getMessage());
            }
        };

        // 15-second delay is safer for Yahoo's rate limits (markets update ~15s anyway)
        scheduler.scheduleWithFixedDelay(task, 0, 15, TimeUnit.SECONDS);
    }
}

