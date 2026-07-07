package com.inventory.observability;

import com.inventory.observability.metrics.BusinessMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessMetricsTest {

    private MeterRegistry registry;
    private BusinessMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new BusinessMetrics(registry);
    }

    @Test
    void recordBorrow_incrementsCounter() {
        metrics.recordBorrow();
        metrics.recordBorrow();

        double count = registry.counter("inventory.items.borrowed.total", "service", "inventory-api").count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void recordReturn_incrementsCounter() {
        metrics.recordReturn();

        double count = registry.counter("inventory.items.returned.total", "service", "inventory-api").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void activeLoans_tracksNetBorrowMinusReturn() {
        metrics.recordBorrow();
        metrics.recordBorrow();
        metrics.recordBorrow();
        metrics.recordReturn();

        double active = registry.get("inventory.loans.active").gauge().value();
        assertThat(active).isEqualTo(2.0);
    }

    @Test
    void recordRegistration_incrementsCounter() {
        metrics.recordRegistration();

        double count = registry.counter("inventory.users.registered.total", "service", "inventory-api").count();
        assertThat(count).isEqualTo(1.0);
    }
}
