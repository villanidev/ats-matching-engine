package com.villanidev.atsmatchingengine.elt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EltScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EltScheduler.class);

    private final EltPipelineService pipelineService;
    private final boolean enabled;

    public EltScheduler(
            EltPipelineService pipelineService,
            @Value("${elt.scheduler.enabled:true}") boolean enabled) {
        this.pipelineService = pipelineService;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${elt.scheduler.cron}", zone = "${elt.scheduler.timezone:UTC}")
    public void runScheduledEtl() {
        if (!enabled) {
            logger.info("ELT scheduler disabled. Skipping run.");
            return;
        }
        pipelineService.runScheduled();
    }
}
