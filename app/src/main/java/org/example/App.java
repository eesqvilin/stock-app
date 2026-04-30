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

    public static void main(String[] args) {

        StockFetcher fetcher = new StockFetcher();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            try {
                StockSnapshot snapshot = fetcher.fetch();
                queue.add(snapshot);
                System.out.println("Added " + snapshot);
            } catch (Exception e) {
                System.out.println("Fetch failed: "  + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
    }

}

