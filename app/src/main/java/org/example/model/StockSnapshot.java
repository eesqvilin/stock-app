package org.example.model;

import java.math.BigDecimal;
import java.time.Instant;

public record StockSnapshot(
        Instant timestamp,
        BigDecimal price
) {}
