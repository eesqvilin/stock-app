package org.example;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.*;


public class App {

    private static final Queue<Stock> stocks = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    }



}

