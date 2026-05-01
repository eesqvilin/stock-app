package org.example;

import org.example.model.StockSnapshot;
import org.example.service.StockFetcher;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class App {

    private static final Queue<StockSnapshot> queue =
            new ConcurrentLinkedQueue<>();
    
    private static final String DOW_JONES_SYMBOL = "^DJI";

    public static void main(String[] args) {

        StockFetcher fetcher = new StockFetcher();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                StockSnapshot snapshot = fetcher.fetch(DOW_JONES_SYMBOL);
                queue.add(snapshot);
                System.out.println("Added " + snapshot);
            } catch (Exception e) {
                System.out.println("Fetch failed: "  + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 60, TimeUnit.SECONDS);
    }

}
