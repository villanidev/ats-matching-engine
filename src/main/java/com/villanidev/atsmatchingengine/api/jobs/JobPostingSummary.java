package com.villanidev.atsmatchingengine.api.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import java.time.LocalDateTime;

public class JobPostingSummary {

    private Long id;

    private String source;

    @JsonProperty("external_id")
    private String externalId;

    private String title;

    private String company;

    private String location;

    @JsonProperty("normalized_at")
    private LocalDateTime normalizedAt;

    public JobPostingSummary() {
    }

    public JobPostingSummary(JobPostingNormalized normalized) {
        this.id = normalized.getId();
        this.source = normalized.getSource();
        this.externalId = normalized.getExternalId();
        this.title = normalized.getTitle();
        this.company = normalized.getCompany();
        this.location = normalized.getLocation();
        this.normalizedAt = normalized.getNormalizedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getNormalizedAt() {
        return normalizedAt;
    }

    public void setNormalizedAt(LocalDateTime normalizedAt) {
        this.normalizedAt = normalizedAt;
    }
}
