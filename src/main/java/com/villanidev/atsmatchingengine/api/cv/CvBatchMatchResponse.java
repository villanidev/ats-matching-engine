package com.villanidev.atsmatchingengine.api.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.cv.CvBatchMatchingService;
import java.util.List;

public class CvBatchMatchResponse {

    @JsonProperty("total_jobs")
    private int totalJobs;

    private int generated;

    @JsonProperty("skipped_existing")
    private int skippedExisting;

    @JsonProperty("skipped_below_threshold")
    private int skippedBelowThreshold;

    private int failed;

    @JsonProperty("generated_ids")
    private List<Long> generatedIds;

    public CvBatchMatchResponse() {
    }

    public CvBatchMatchResponse(CvBatchMatchingService.BatchResult result) {
        this.totalJobs = result.getTotalJobs();
        this.generated = result.getGenerated();
        this.skippedExisting = result.getSkippedExisting();
        this.skippedBelowThreshold = result.getSkippedBelowThreshold();
        this.failed = result.getFailed();
        this.generatedIds = result.getGeneratedIds();
    }

    public int getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(int totalJobs) {
        this.totalJobs = totalJobs;
    }

    public int getGenerated() {
        return generated;
    }

    public void setGenerated(int generated) {
        this.generated = generated;
    }

    public int getSkippedExisting() {
        return skippedExisting;
    }

    public void setSkippedExisting(int skippedExisting) {
        this.skippedExisting = skippedExisting;
    }

    public int getSkippedBelowThreshold() {
        return skippedBelowThreshold;
    }

    public void setSkippedBelowThreshold(int skippedBelowThreshold) {
        this.skippedBelowThreshold = skippedBelowThreshold;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<Long> getGeneratedIds() {
        return generatedIds;
    }

    public void setGeneratedIds(List<Long> generatedIds) {
        this.generatedIds = generatedIds;
    }
}
