package com.villanidev.atsmatchingengine.elt;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecution;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionItem;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionItemStatus;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionService;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionStatus;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingNormalizedRepository;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingRawRepository;
import com.villanidev.atsmatchingengine.elt.scraping.JobPortalScraper;
import com.villanidev.atsmatchingengine.elt.scraping.JobPortalScraperRegistry;
import com.villanidev.atsmatchingengine.elt.scraping.PortalConfig;
import com.villanidev.atsmatchingengine.elt.scraping.PortalConfigRepository;
import com.villanidev.atsmatchingengine.cv.CvBatchMatchingService;
import com.villanidev.atsmatchingengine.cv.storage.CvMasterStoreService;
import com.villanidev.atsmatchingengine.domain.Options;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final EltExecutionService executionService;
    private final CvBatchMatchingService batchMatchingService;
    private final CvMasterStoreService cvMasterStoreService;
    private final boolean autoMatchingEnabled;
    private final int autoMatchingLimit;

    public EltPipelineService(
            JobPostingRawRepository rawRepository,
            JobPostingNormalizedRepository normalizedRepository,
            JobNormalizationService normalizationService,
            JobPortalScraperRegistry scraperRegistry,
            PortalConfigRepository portalConfigRepository,
            @Qualifier("eltTaskExecutor") Executor eltExecutor,
            EltExecutionService executionService,
            CvBatchMatchingService batchMatchingService,
            CvMasterStoreService cvMasterStoreService,
            @Value("${elt.matching.auto.enabled:true}") boolean autoMatchingEnabled,
            @Value("${elt.matching.auto.limit:200}") int autoMatchingLimit) {
        this.rawRepository = rawRepository;
        this.normalizedRepository = normalizedRepository;
        this.normalizationService = normalizationService;
        this.scraperRegistry = scraperRegistry;
        this.portalConfigRepository = portalConfigRepository;
        this.eltExecutor = eltExecutor;
        this.executionService = executionService;
        this.batchMatchingService = batchMatchingService;
        this.cvMasterStoreService = cvMasterStoreService;
        this.autoMatchingEnabled = autoMatchingEnabled;
        this.autoMatchingLimit = autoMatchingLimit;
    }

    public void runScheduled() {
        EltExecution execution = executionService.startExecution("SCHEDULED", "ALL");
        logger.info("ELT scheduled run started executionId={}", execution.getId());
        try {
            ScrapeSummary summary = extractAllSources(execution);
            List<JobPostingRaw> extracted = summary.items;
            loadRawData(extracted);
            executionService.updateCounts(execution, extracted.size(), null, null);
            int normalizedCount = normalizeData();
            executionService.updateCounts(execution, null, normalizedCount, null);
            triggerAutoMatchingIfEnabled(execution);
            executionService.finishExecution(execution, summary.status, null);
            logger.info("ELT scheduled run finished executionId={}", execution.getId());
        } catch (Exception ex) {
            executionService.finishExecution(execution, EltExecutionStatus.FAILED, ex.getMessage());
            logger.info("ELT scheduled run failed executionId={} message={}", execution.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Async("eltTaskExecutor")
    public CompletableFuture<Void> runOnDemandAll() {
        EltExecution execution = executionService.startExecution("ON_DEMAND", "ALL");
        logger.info("ELT on-demand run started for all sources executionId={}", execution.getId());
        try {
            ScrapeSummary summary = extractAllSources(execution);
            List<JobPostingRaw> extracted = summary.items;
            loadRawData(extracted);
            executionService.updateCounts(execution, extracted.size(), null, null);
            int normalizedCount = normalizeData();
            executionService.updateCounts(execution, null, normalizedCount, null);
            triggerAutoMatchingIfEnabled(execution);
            executionService.finishExecution(execution, summary.status, null);
            logger.info("ELT on-demand run finished for all sources executionId={}", execution.getId());
        } catch (Exception ex) {
            executionService.finishExecution(execution, EltExecutionStatus.FAILED, ex.getMessage());
            logger.info("ELT on-demand run failed for all sources executionId={} message={}", execution.getId(), ex.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async("eltTaskExecutor")
    public CompletableFuture<Void> runOnDemand(String sourceId) {
        EltExecution execution = executionService.startExecution("ON_DEMAND", sourceId != null ? sourceId : "UNKNOWN");
        logger.info("ELT on-demand run started for source={} executionId={}", sourceId, execution.getId());
        try {
            ScrapeSummary summary = extractSource(execution, sourceId);
            List<JobPostingRaw> extracted = summary.items;
            loadRawData(extracted);
            executionService.updateCounts(execution, extracted.size(), null, null);
            int normalizedCount = normalizeData();
            executionService.updateCounts(execution, null, normalizedCount, null);
            triggerAutoMatchingIfEnabled(execution);
            executionService.finishExecution(execution, summary.status, summary.message);
            logger.info("ELT on-demand run finished for source={} executionId={}", sourceId, execution.getId());
        } catch (Exception ex) {
            executionService.finishExecution(execution, EltExecutionStatus.FAILED, ex.getMessage());
            logger.info("ELT on-demand run failed for source={} executionId={} message={}", sourceId, execution.getId(), ex.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    private ScrapeSummary extractAllSources(EltExecution execution) {
        List<PortalConfig> configs = portalConfigRepository.findByEnabledTrue();
        if (configs.isEmpty()) {
            logger.info("No portal configs found for scraping");
            return ScrapeSummary.success(Collections.emptyList());
        }
        List<CompletableFuture<ScrapeResult>> futures = configs.stream()
                .filter(PortalConfig::isEnabled)
                .map(config -> CompletableFuture.supplyAsync(() -> scrapeWithTracking(execution, config), eltExecutor))
                .collect(Collectors.toList());
        if (futures.isEmpty()) {
            logger.info("No enabled portal configs found for scraping");
            return ScrapeSummary.success(Collections.emptyList());
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        boolean anyFailed = futures.stream().anyMatch(f -> f.join().failed);
        boolean anySuccess = futures.stream().anyMatch(f -> !f.join().failed);
        EltExecutionStatus status = anyFailed && anySuccess
                ? EltExecutionStatus.PARTIAL
                : (anyFailed ? EltExecutionStatus.FAILED : EltExecutionStatus.SUCCESS);
        List<JobPostingRaw> items = futures.stream()
                .flatMap(future -> future.join().items.stream())
                .collect(Collectors.toList());
        logger.info("Extracted {} RAW items from all sources", items.size());
        return new ScrapeSummary(items, status, null);
    }

    private ScrapeSummary extractSource(EltExecution execution, String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            logger.info("SourceId not provided, skipping extraction");
            return new ScrapeSummary(Collections.emptyList(), EltExecutionStatus.FAILED, "SourceId not provided");
        }
        PortalConfig config = portalConfigRepository.findByPortalIdIgnoreCase(sourceId).orElse(null);
        if (config == null) {
            logger.info("No portal config found for sourceId={}", sourceId);
            return new ScrapeSummary(Collections.emptyList(), EltExecutionStatus.FAILED, "Config not found");
        }
        if (!config.isEnabled()) {
            logger.info("Portal sourceId={} disabled; skipping extraction", sourceId);
            return new ScrapeSummary(Collections.emptyList(), EltExecutionStatus.FAILED, "Config disabled");
        }
        ScrapeResult result = scrapeWithTracking(execution, config);
        List<JobPostingRaw> items = result.items;
        logger.info("Extracted {} RAW items from source={}", items.size(), sourceId);
        return result.failed
                ? new ScrapeSummary(Collections.emptyList(), EltExecutionStatus.FAILED, result.message)
                : new ScrapeSummary(items, EltExecutionStatus.SUCCESS, null);
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

    private ScrapeResult scrapeWithTracking(EltExecution execution, PortalConfig config) {
        EltExecutionItem item = executionService.startItem(execution, config.getPortalId());
        try {
            List<JobPostingRaw> items = scrapeFromConfig(config);
            executionService.completeItem(item, EltExecutionItemStatus.SUCCESS, items.size(), null);
            return ScrapeResult.success(items);
        } catch (Exception ex) {
            executionService.completeItem(item, EltExecutionItemStatus.FAILED, 0, ex.getMessage());
            logger.info("Scrape failed for portalId={} message={}", config.getPortalId(), ex.getMessage());
            return ScrapeResult.failure(ex.getMessage());
        }
    }

    private static class ScrapeResult {
        private final List<JobPostingRaw> items;
        private final boolean failed;
        private final String message;

        private ScrapeResult(List<JobPostingRaw> items, boolean failed, String message) {
            this.items = items;
            this.failed = failed;
            this.message = message;
        }

        static ScrapeResult success(List<JobPostingRaw> items) {
            return new ScrapeResult(items, false, null);
        }

        static ScrapeResult failure(String message) {
            return new ScrapeResult(Collections.emptyList(), true, message);
        }
    }

    private static class ScrapeSummary {
        private final List<JobPostingRaw> items;
        private final EltExecutionStatus status;
        private final String message;

        private ScrapeSummary(List<JobPostingRaw> items, EltExecutionStatus status, String message) {
            this.items = items;
            this.status = status;
            this.message = message;
        }

        static ScrapeSummary success(List<JobPostingRaw> items) {
            return new ScrapeSummary(items, EltExecutionStatus.SUCCESS, null);
        }
    }

    private void loadRawData(List<JobPostingRaw> rawItems) {
        if (rawItems == null || rawItems.isEmpty()) {
            logger.info("No RAW data to load");
            return;
        }
        List<JobPostingRaw> toPersist = rawItems.stream()
                .filter(this::isNotDuplicate)
                .toList();
        if (toPersist.isEmpty()) {
            logger.info("All RAW items already exist. Skipping load.");
            return;
        }
        toPersist.forEach(item -> {
            if (item.getFetchedAt() == null) {
                item.setFetchedAt(LocalDateTime.now());
            }
        });
        rawRepository.saveAll(toPersist);
        logger.info("Loaded {} RAW items", toPersist.size());
    }

    private boolean isNotDuplicate(JobPostingRaw item) {
        if (item == null) {
            return false;
        }
        String source = item.getSource();
        String externalId = item.getExternalId();
        String url = item.getUrl();
        if (source != null && !source.isBlank() && externalId != null && !externalId.isBlank()) {
            if (rawRepository.existsBySourceAndExternalId(source, externalId)) {
                return false;
            }
        }
        if (url != null && !url.isBlank()) {
            return !rawRepository.existsByUrl(url);
        }
        return true;
    }

    private int normalizeData() {
        List<JobPostingRaw> rawItems = rawRepository.findByNormalizedFalse();
        if (rawItems.isEmpty()) {
            logger.info("No RAW data pending normalization");
            return 0;
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
        return normalizedCount;
    }

    private void triggerAutoMatchingIfEnabled(EltExecution execution) {
        if (!autoMatchingEnabled) {
            return;
        }
        List<Long> cvMasterIds = cvMasterStoreService.listIds();
        if (cvMasterIds.isEmpty()) {
            return;
        }
        Options options = new Options();
        int generatedTotal = 0;
        for (Long cvMasterId : cvMasterIds) {
            CvBatchMatchingService.BatchResult result = batchMatchingService.runBatch(
                    cvMasterId, options, autoMatchingLimit);
            generatedTotal += result.getGenerated();
        }
        executionService.updateCounts(execution, null, null, generatedTotal);
    }
}
