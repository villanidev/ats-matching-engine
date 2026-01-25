package com.villanidev.atsmatchingengine.api.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedEntity;
import java.time.LocalDateTime;

public class CvGeneratedSummary {

    private Long id;

    @JsonProperty("cv_master_id")
    private Long cvMasterId;

    @JsonProperty("job_posting_id")
    private Long jobPostingId;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_location")
    private String jobLocation;

    @JsonProperty("matching_score")
    private Double matchingScore;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public CvGeneratedSummary() {
    }

    public CvGeneratedSummary(CvGeneratedEntity entity) {
        this.id = entity.getId();
        this.cvMasterId = entity.getCvMasterId();
        this.jobPostingId = entity.getJobPostingId();
        this.jobTitle = entity.getJobTitle();
        this.jobLocation = entity.getJobLocation();
        this.matchingScore = entity.getMatchingScore();
        this.createdAt = entity.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCvMasterId() {
        return cvMasterId;
    }

    public void setCvMasterId(Long cvMasterId) {
        this.cvMasterId = cvMasterId;
    }

    public Long getJobPostingId() {
        return jobPostingId;
    }

    public void setJobPostingId(Long jobPostingId) {
        this.jobPostingId = jobPostingId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public Double getMatchingScore() {
        return matchingScore;
    }

    public void setMatchingScore(Double matchingScore) {
        this.matchingScore = matchingScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
