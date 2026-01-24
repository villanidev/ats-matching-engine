package com.villanidev.atsmatchingengine.elt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting_raw")
public class JobPostingRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    private String externalId;

    private String url;

    @Column(columnDefinition = "TEXT")
    private String rawContent;

    private LocalDateTime fetchedAt;

    @Column(nullable = false)
    private boolean normalized = false;

    private LocalDateTime normalizedAt;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public LocalDateTime getNormalizedAt() {
        return normalizedAt;
    }

    public void setNormalizedAt(LocalDateTime normalizedAt) {
        this.normalizedAt = normalizedAt;
    }
}
