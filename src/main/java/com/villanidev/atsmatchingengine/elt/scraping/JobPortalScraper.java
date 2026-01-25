package com.villanidev.atsmatchingengine.elt.scraping;

import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JobPortalScraper {

    Logger LOGGER = LoggerFactory.getLogger(JobPortalScraper.class);

    String getPortalId();

    List<JobPostingRaw> scrape(PortalConfig config);

    default List<JobPostingRaw> scrapeWithRetry(PortalConfig config, Supplier<List<JobPostingRaw>> operation) {
        int retries = config.getMaxRetries() != null ? config.getMaxRetries() : 2;
        for (int attempt = 1; attempt <= retries + 1; attempt++) {
            try {
                sleepIfNeeded(config);
                return operation.get();
            } catch (Exception ex) {
                onRetry(ex, attempt, retries);
                backoff(attempt);
            }
        }
        return List.of();
    }

    default void sleepIfNeeded(PortalConfig config) throws InterruptedException {
        if (config.getRateLimitMs() != null && config.getRateLimitMs() > 0) {
            Thread.sleep(config.getRateLimitMs());
        }
    }

    default void backoff(int attempt) {
        try {
            Thread.sleep(Math.min(2000L, attempt * 500L));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    default void onRetry(Exception ex, int attempt, int maxRetries) {
        LOGGER.info("Retry portal={} class={} attempt={}/{} message={}",
                getPortalId(), getClass().getSimpleName(), attempt, maxRetries + 1, ex.getMessage());
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
