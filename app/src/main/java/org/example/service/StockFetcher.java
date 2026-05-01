package org.example.service;

import org.example.model.StockSnapshot;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;


public class StockFetcher {
    
    public StockSnapshot fetch() throws IOException {
        Stock stock = YahooFinance.get("^DJI");
        if (stock == null || stock.getQuote().getPrice() == null) {
            throw new IOException("Stock not found");
        }
            double price = stock.getQuote().getPrice().doubleValue();
            return new StockSnapshot(Instant.now(), price);


    }
}