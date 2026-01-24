package com.villanidev.atsmatchingengine.elt;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingNormalizedRepository;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingRawRepository;
import com.villanidev.atsmatchingengine.elt.scraping.JobPortalScraper;
import com.villanidev.atsmatchingengine.elt.scraping.JobPortalScraperRegistry;
import com.villanidev.atsmatchingengine.elt.scraping.PortalConfig;
import com.villanidev.atsmatchingengine.elt.scraping.PortalConfigRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EltPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(EltPipelineService.class);

    private final JobPostingRawRepository rawRepository;
    private final JobPostingNormalizedRepository normalizedRepository;
    private final JobNormalizationService normalizationService;
    private final JobPortalScraperRegistry scraperRegistry;
    private final PortalConfigRepository portalConfigRepository;
    private final Executor eltExecutor;

    public EltPipelineService(
            JobPostingRawRepository rawRepository,
            JobPostingNormalizedRepository normalizedRepository,
            JobNormalizationService normalizationService,
            JobPortalScraperRegistry scraperRegistry,
            PortalConfigRepository portalConfigRepository,
            @Qualifier("eltTaskExecutor") Executor eltExecutor) {
        this.rawRepository = rawRepository;
        this.normalizedRepository = normalizedRepository;
        this.normalizationService = normalizationService;
        this.scraperRegistry = scraperRegistry;
        this.portalConfigRepository = portalConfigRepository;
        this.eltExecutor = eltExecutor;
    }

    public void runScheduled() {
        logger.info("ELT scheduled run started");
        List<JobPostingRaw> extracted = extractAllSources();
        loadRawData(extracted);
        normalizeData();
        logger.info("ELT scheduled run finished");
    }

    @Async("eltTaskExecutor")
    public CompletableFuture<Void> runOnDemandAll() {
        logger.info("ELT on-demand run started for all sources");
        List<JobPostingRaw> extracted = extractAllSources();
        loadRawData(extracted);
        normalizeData();
        logger.info("ELT on-demand run finished for all sources");
        return CompletableFuture.completedFuture(null);
    }

    @Async("eltTaskExecutor")
    public CompletableFuture<Void> runOnDemand(String sourceId) {
        logger.info("ELT on-demand run started for source={}", sourceId);
        List<JobPostingRaw> extracted = extractSource(sourceId);
        loadRawData(extracted);
        normalizeData();
        logger.info("ELT on-demand run finished for source={}", sourceId);
        return CompletableFuture.completedFuture(null);
    }

    private List<JobPostingRaw> extractAllSources() {
        List<PortalConfig> configs = portalConfigRepository.findByEnabledTrue();
        if (configs.isEmpty()) {
            logger.info("No portal configs found for scraping");
            return Collections.emptyList();
        }
        List<CompletableFuture<List<JobPostingRaw>>> futures = configs.stream()
                .filter(PortalConfig::isEnabled)
                .map(config -> CompletableFuture.supplyAsync(() -> scrapeFromConfig(config), eltExecutor)
                        .exceptionally(ex -> {
                            logger.info("Scrape failed for portalId={} message={}", config.getPortalId(), ex.getMessage());
                            return Collections.emptyList();
                        }))
                .collect(Collectors.toList());
        if (futures.isEmpty()) {
            logger.info("No enabled portal configs found for scraping");
            return Collections.emptyList();
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<JobPostingRaw> items = futures.stream()
                .flatMap(future -> future.join().stream())
                .collect(Collectors.toList());
        logger.info("Extracted {} RAW items from all sources", items.size());
        return items;
    }

    private List<JobPostingRaw> extractSource(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            logger.info("SourceId not provided, skipping extraction");
            return Collections.emptyList();
        }
        PortalConfig config = portalConfigRepository.findByPortalIdIgnoreCase(sourceId).orElse(null);
        if (config == null) {
            logger.info("No portal config found for sourceId={}", sourceId);
            return Collections.emptyList();
        }
        if (!config.isEnabled()) {
            logger.info("Portal sourceId={} disabled; skipping extraction", sourceId);
            return Collections.emptyList();
        }
        List<JobPostingRaw> items = scrapeFromConfig(config);
        logger.info("Extracted {} RAW items from source={}", items.size(), sourceId);
        return items;
    }

    private List<JobPostingRaw> scrapeFromConfig(PortalConfig config) {
        JobPortalScraper scraper = scraperRegistry.findById(config.getPortalId()).orElse(null);
        if (scraper == null) {
            logger.info("No scraper registered for portal id={}", config.getPortalId());
            return Collections.emptyList();
        }
        List<JobPostingRaw> items = scraper.scrape(config);
        items.forEach(item -> {
            if (item.getSource() == null || item.getSource().isBlank()) {
                item.setSource(config.getPortalId());
            }
        });
        return items;
    }

    private void loadRawData(List<JobPostingRaw> rawItems) {
        if (rawItems == null || rawItems.isEmpty()) {
            logger.info("No RAW data to load");
            return;
        }
        rawItems.forEach(item -> {
            if (item.getFetchedAt() == null) {
                item.setFetchedAt(LocalDateTime.now());
            }
        });
        rawRepository.saveAll(rawItems);
        logger.info("Loaded {} RAW items", rawItems.size());
    }

    private void normalizeData() {
        List<JobPostingRaw> rawItems = rawRepository.findByNormalizedFalse();
        if (rawItems.isEmpty()) {
            logger.info("No RAW data pending normalization");
            return;
        }
        int normalizedCount = 0;
        for (JobPostingRaw raw : rawItems) {
            JobPostingNormalized normalized = normalizationService.normalize(raw).orElse(null);
            if (normalized != null) {
                normalizedRepository.save(normalized);
                raw.setNormalized(true);
                raw.setNormalizedAt(LocalDateTime.now());
                rawRepository.save(raw);
                normalizedCount++;
            }
        }
        logger.info("Normalized {} RAW items", normalizedCount);
    }
}
