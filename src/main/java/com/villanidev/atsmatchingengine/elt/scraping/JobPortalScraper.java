package com.villanidev.atsmatchingengine.elt.scraping;

import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.util.List;

public interface JobPortalScraper {

    String getPortalId();

    List<JobPostingRaw> scrape(PortalConfig config);
}
