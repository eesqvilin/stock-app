package org.example;

import java.time.Instant;

public record StockSnapshot(
        Instant timestamp,
        double price
) {}
