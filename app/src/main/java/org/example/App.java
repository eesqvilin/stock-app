package org.example;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    private static final Queue<StockSnapshot> queue = new ConcurrentLinkedQueue<>();
    private static final String DOW_JONES_SYMBOL = "^DJI";

    public static record StockSnapshot(
            Instant timestamp,
            BigDecimal price
    ) {}

    public static class StockFetcher {
        public StockSnapshot fetch(String symbol) throws IOException {
            Stock stock = YahooFinance.get(symbol);
            if (stock == null || stock.getQuote().getPrice() == null) {
                throw new IOException("Stock not found or price is not available for: " + symbol);
            }
            BigDecimal price = stock.getQuote().getPrice();
            return new StockSnapshot(Instant.now(), price);
        }
    }

    public static void main(String[] args) {
        StockFetcher fetcher = new StockFetcher();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            try {
                StockSnapshot snapshot = fetcher.fetch(DOW_JONES_SYMBOL);
                queue.add(snapshot);
                System.out.println("Added " + snapshot);
            } catch (Exception e) {
                System.out.println("Fetch failed: " + e.getMessage());
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
    }
}
