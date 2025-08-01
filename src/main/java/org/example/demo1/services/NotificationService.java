package org.example.demo1.services;
import io.github.resilience4j.retry.*;
import io.github.resilience4j.circuitbreaker.*;
import io.vavr.control.Try;
import org.slf4j.*;
import java.time.Duration;
import java.util.Random;
import java.util.function.Supplier;
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final Random random = new Random();
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public NotificationService() {
        // Day 3: Retry Config
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(5))
                .retryExceptions(RuntimeException.class)
                .build();
        retry = Retry.of("notificationRetry", retryConfig);

        // Day 4: Circuit Breaker Config
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .slidingWindowSize(4)
                .permittedNumberOfCallsInHalfOpenState(2)
                .recordExceptions(RuntimeException.class)
                .build();
        circuitBreaker = CircuitBreaker.of("notificationCB", cbConfig);

        // Log state changes and open circuit behavior
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> logger.warn("CircuitBreaker state changed: {}", event.getStateTransition()))
                .onCallNotPermitted(event -> logger.warn("Call not permitted: Circuit Breaker is OPEN"));
    }
    public String sendNotifications(String userId, String message) {
        // Day 2: Add context for observability
        MDC.put("userId", userId);
        Supplier<String> decorated = CircuitBreaker
                .decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry,
                        () -> simulateExternalNotification(userId, message)));

        // Day 1 & 5: Logging around resilience pattern
        Try<String> result = Try.ofSupplier(decorated)
                .onFailure(e -> logger.warn("Notification sending failed, attempting fallback", e))
                .recover(e -> fallback(userId, e));

        MDC.clear();
        return result.get();
    }
    // Day 5: Fallback method if all retries fail or CB is OPEN
    public String fallback(String userId, Throwable e) {
        logger.warn("Fallback triggered for user: {} due to {}", userId, e.getMessage());
        return "Notification failed. Fallback executed for user " + userId;
    }
    // Day 5: Simulated API latency and random failure for demo
    private String simulateExternalNotification(String to, String message) {
        logger.info("Attempting to send notification to '{}'", to);
        // Simulate latency
        try {
            int delay = 1000 + random.nextInt(1000);
            logger.debug("Simulated delay: {} ms", delay);
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}
        // Simulate random failure (60% chance)
        if (random.nextInt(10) < 6) {
            logger.warn("Simulated failure for user {}", to);
            throw new RuntimeException("Simulated external service failure");
        }
        logger.info("Notification sent successfully to '{}'", to);
        return "Notification sent to " + to;
    }
}