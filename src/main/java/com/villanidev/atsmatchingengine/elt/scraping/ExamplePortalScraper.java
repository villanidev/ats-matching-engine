package com.villanidev.atsmatchingengine.elt.scraping;

import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExamplePortalScraper implements JobPortalScraper {

    private static final Logger logger = LoggerFactory.getLogger(ExamplePortalScraper.class);

    @Override
    public String getPortalId() {
        return "example-portal";
    }

    @Override
    public List<JobPostingRaw> scrape(PortalConfig config) {
        logger.info("Scraping portal={} listingUrl={} (stub)", config.getPortalId(), config.getListingUrl());
        return Collections.emptyList();
    }
}
