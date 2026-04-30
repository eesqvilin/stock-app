package org.example.model;

import java.time.Instant;

public record StockSnapshot(
        Instant timestamp,
        double price
) {}
