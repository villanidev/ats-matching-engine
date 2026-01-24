package com.villanidev.atsmatchingengine.elt.scraping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JobPortalScraperRegistry {

    private final Map<String, JobPortalScraper> scrapersById = new HashMap<>();

    public JobPortalScraperRegistry(List<JobPortalScraper> scrapers) {
        for (JobPortalScraper scraper : scrapers) {
            scrapersById.put(scraper.getPortalId(), scraper);
        }
    }

    public Optional<JobPortalScraper> findById(String id) {
        return Optional.ofNullable(scrapersById.get(id));
    }
}
