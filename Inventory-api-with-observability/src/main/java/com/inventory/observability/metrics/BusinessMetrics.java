package com.inventory.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics published to CloudWatch.
 *
 * These metrics go beyond infrastructure monitoring (CPU, memory) to track
 * actual business KPIs that matter for the application's purpose.
 */
@Component
public class BusinessMetrics {

    private final Counter borrowCounter;
    private final Counter returnCounter;
    private final Counter registrationCounter;
    private final Timer borrowProcessingTimer;
    private final AtomicInteger activeLoans;

    public BusinessMetrics(MeterRegistry registry) {
        // Counter: total items borrowed (monotonically increasing)
        this.borrowCounter = Counter.builder("inventory.items.borrowed.total")
                .description("Total number of items borrowed")
                .tag("service", "inventory-api")
                .register(registry);

        // Counter: total items returned
        this.returnCounter = Counter.builder("inventory.items.returned.total")
                .description("Total number of items returned")
                .tag("service", "inventory-api")
                .register(registry);

        // Counter: user registrations
        this.registrationCounter = Counter.builder("inventory.users.registered.total")
                .description("Total new user registrations")
                .tag("service", "inventory-api")
                .register(registry);

        // Timer: how long borrow transactions take
        this.borrowProcessingTimer = Timer.builder("inventory.borrow.duration")
                .description("Time taken to process a borrow transaction")
                .tag("service", "inventory-api")
                .register(registry);

        // Gauge: current active loans (goes up and down)
        this.activeLoans = new AtomicInteger(0);
        Gauge.builder("inventory.loans.active", activeLoans, AtomicInteger::get)
                .description("Current number of active (unreturned) loans")
                .tag("service", "inventory-api")
                .register(registry);
    }

    public void recordBorrow() {
        borrowCounter.increment();
        activeLoans.incrementAndGet();
    }

    public void recordReturn() {
        returnCounter.increment();
        activeLoans.decrementAndGet();
    }

    public void recordRegistration() {
        registrationCounter.increment();
    }

    public Timer.Sample startBorrowTimer() {
        return Timer.start();
    }

    public void stopBorrowTimer(Timer.Sample sample) {
        sample.stop(borrowProcessingTimer);
    }
}
