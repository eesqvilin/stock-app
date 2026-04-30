package org.example;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.*;


public class App {

    private static final Queue<StockSnapshot> queue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                Stock stock = YahooFinance.get("^DJI");

                if (stock != null && stock.getQuote().getPrice() != null) {
                    double price = stock.getQuote().getPrice().doubleValue();
                    StockSnapshot snapshot = new StockSnapshot(Instant.now(), price);
                    queue.add(snapshot);
                    System.out.println(snapshot);

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        };
        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
    }



}

