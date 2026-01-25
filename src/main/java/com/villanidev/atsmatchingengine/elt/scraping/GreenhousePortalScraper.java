package com.villanidev.atsmatchingengine.elt.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GreenhousePortalScraper implements JobPortalScraper {

    private static final Logger logger = LoggerFactory.getLogger(GreenhousePortalScraper.class);
    private static final String DEFAULT_USER_AGENT = "ats-matching-engine/1.0";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GreenhousePortalScraper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String getPortalId() {
        return "greenhouse";
    }

    @Override
    public List<JobPostingRaw> scrape(PortalConfig config) {
        String companySlug = extractCompanySlug(config.getListingUrl());
        if (companySlug == null || companySlug.isBlank()) {
            logger.info("Greenhouse scraper: missing company slug. listingUrl={}", config.getListingUrl());
            return Collections.emptyList();
        }
        String jobsUrl = "https://boards-api.greenhouse.io/v1/boards/" + companySlug + "/jobs";
        String userAgent = config.getUserAgent() != null && !config.getUserAgent().isBlank()
                ? config.getUserAgent()
                : DEFAULT_USER_AGENT;
        return scrapeWithRetry(config, () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(jobsUrl))
                        .timeout(Duration.ofSeconds(30))
                        .header("User-Agent", userAgent)
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return parseJobs(response.body(), companySlug);
                }
                logger.info("Greenhouse scraper: non-2xx response status={} url={}", response.statusCode(), jobsUrl);
                return Collections.emptyList();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.info("Greenhouse scraper interrupted for companySlug={} message={}", companySlug, ex.getMessage());
                return Collections.emptyList();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private List<JobPostingRaw> parseJobs(String body, String companySlug) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode jobs = root.get("jobs");
        if (jobs == null || !jobs.isArray()) {
            logger.info("Greenhouse scraper: no jobs array for companySlug={}", companySlug);
            return Collections.emptyList();
        }
        List<JobPostingRaw> items = new ArrayList<>();
        for (JsonNode job : jobs) {
            JobPostingRaw raw = new JobPostingRaw();
            raw.setSource(getPortalId());
            raw.setExternalId(textOrNull(job, "id"));
            raw.setUrl(textOrNull(job, "absolute_url"));
            raw.setRawContent(job.toString());
            raw.setFetchedAt(LocalDateTime.now());
            items.add(raw);
        }
        logger.info("Greenhouse scraper: extracted {} jobs for companySlug={}", items.size(), companySlug);
        return items;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text != null && !text.isBlank() ? text : null;
    }

    private String extractCompanySlug(String listingUrl) {
        if (listingUrl == null || listingUrl.isBlank()) {
            return null;
        }
        String normalized = listingUrl.trim();
        String marker = "boards.greenhouse.io/";
        int idx = normalized.indexOf(marker);
        if (idx < 0) {
            marker = "greenhouse.io/";
            idx = normalized.indexOf(marker);
        }
        if (idx < 0) {
            return null;
        }
        String tail = normalized.substring(idx + marker.length());
        int endIdx = tail.indexOf('/');
        if (endIdx >= 0) {
            tail = tail.substring(0, endIdx);
        }
        int qIdx = tail.indexOf('?');
        if (qIdx >= 0) {
            tail = tail.substring(0, qIdx);
        }
        return tail.trim();
    }

}
