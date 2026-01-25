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
public class BreezyPortalScraper implements JobPortalScraper {

    private static final Logger logger = LoggerFactory.getLogger(BreezyPortalScraper.class);
    private static final String DEFAULT_USER_AGENT = "ats-matching-engine/1.0";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public BreezyPortalScraper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String getPortalId() {
        return "breezy";
    }

    @Override
    public List<JobPostingRaw> scrape(PortalConfig config) {
        String companySlug = extractCompanySlug(config.getListingUrl());
        if (companySlug == null || companySlug.isBlank()) {
            logger.info("Breezy scraper: missing company slug. listingUrl={}", config.getListingUrl());
            return Collections.emptyList();
        }
        String jobsUrl = "https://" + companySlug + ".breezy.hr/jobs.json";
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
                logger.info("Breezy scraper: non-2xx response status={} url={}", response.statusCode(), jobsUrl);
                return Collections.emptyList();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.info("Breezy scraper interrupted for companySlug={} message={}", companySlug, ex.getMessage());
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
            logger.info("Breezy scraper: no jobs array for companySlug={}", companySlug);
            return Collections.emptyList();
        }
        List<JobPostingRaw> items = new ArrayList<>();
        for (JsonNode job : jobs) {
            JobPostingRaw raw = new JobPostingRaw();
            raw.setSource(getPortalId());
            raw.setExternalId(textOrNull(job, "id"));
            raw.setUrl(textOrNull(job, "url"));
            raw.setRawContent(job.toString());
            raw.setFetchedAt(LocalDateTime.now());
            items.add(raw);
        }
        logger.info("Breezy scraper: extracted {} jobs for companySlug={}", items.size(), companySlug);
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
        try {
            URI uri = URI.create(listingUrl.trim());
            String host = uri.getHost();
            if (host != null && host.endsWith(".breezy.hr")) {
                return host.replace(".breezy.hr", "");
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
        String normalized = listingUrl.trim();
        int idx = normalized.indexOf(".breezy.hr");
        if (idx > 0) {
            String host = normalized.substring(0, idx);
            int protocolIdx = host.indexOf("//");
            if (protocolIdx >= 0) {
                host = host.substring(protocolIdx + 2);
            }
            return host;
        }
        return null;
    }

}
