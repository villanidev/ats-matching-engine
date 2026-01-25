package com.villanidev.atsmatchingengine.elt.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AshbyPortalScraper implements JobPortalScraper {

    private static final Logger logger = LoggerFactory.getLogger(AshbyPortalScraper.class);
    private static final String DEFAULT_USER_AGENT = "ats-matching-engine/1.0";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AshbyPortalScraper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String getPortalId() {
        return "ashby";
    }

    @Override
    public List<JobPostingRaw> scrape(PortalConfig config) {
        String listingUrl = config.getListingUrl();
        String orgSlug = extractOrgSlug(listingUrl);
        if (orgSlug == null || orgSlug.isBlank()) {
            logger.info("Ashby scraper: missing organization slug. listingUrl={}", listingUrl);
            return Collections.emptyList();
        }
        String apiUrl = "https://jobs.ashbyhq.com/api/non-user-portal/jobs?organizationSlug="
                + URLEncoder.encode(orgSlug, StandardCharsets.UTF_8);
        String userAgent = config.getUserAgent() != null && !config.getUserAgent().isBlank()
                ? config.getUserAgent()
                : DEFAULT_USER_AGENT;
        return scrapeWithRetry(config, () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(30))
                        .header("User-Agent", userAgent)
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return parseJobs(response.body(), orgSlug);
                }
                logger.info("Ashby scraper: non-2xx response status={} url={}", response.statusCode(), apiUrl);
                return Collections.emptyList();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.info("Ashby scraper interrupted for orgSlug={} message={}", orgSlug, ex.getMessage());
                return Collections.emptyList();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private List<JobPostingRaw> parseJobs(String body, String orgSlug) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode jobs = root.get("jobs");
        if (jobs == null || !jobs.isArray()) {
            logger.info("Ashby scraper: no jobs array for orgSlug={}", orgSlug);
            return Collections.emptyList();
        }
        List<JobPostingRaw> items = new ArrayList<>();
        for (JsonNode job : jobs) {
            JobPostingRaw raw = new JobPostingRaw();
            raw.setSource(getPortalId());
            raw.setExternalId(textOrNull(job, "id"));
            raw.setUrl(buildJobUrl(orgSlug, raw.getExternalId(), textOrNull(job, "jobUrl")));
            raw.setRawContent(job.toString());
            raw.setFetchedAt(LocalDateTime.now());
            items.add(raw);
        }
        logger.info("Ashby scraper: extracted {} jobs for orgSlug={}", items.size(), orgSlug);
        return items;
    }

    private String buildJobUrl(String orgSlug, String id, String jobUrl) {
        if (jobUrl != null && !jobUrl.isBlank()) {
            return jobUrl;
        }
        if (id == null || id.isBlank()) {
            return null;
        }
        return "https://jobs.ashbyhq.com/" + orgSlug + "/" + id;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text != null && !text.isBlank() ? text : null;
    }

    private String extractOrgSlug(String listingUrl) {
        if (listingUrl == null || listingUrl.isBlank()) {
            return null;
        }
        String normalized = listingUrl.trim();
        int idx = normalized.indexOf("jobs.ashbyhq.com/");
        if (idx < 0) {
            return null;
        }
        String tail = normalized.substring(idx + "jobs.ashbyhq.com/".length());
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
