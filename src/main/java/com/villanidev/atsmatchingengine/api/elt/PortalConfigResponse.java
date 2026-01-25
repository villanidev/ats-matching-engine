package com.villanidev.atsmatchingengine.api.elt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.elt.scraping.PortalConfig;
import java.time.LocalDateTime;

public class PortalConfigResponse {

    private Long id;

    @JsonProperty("portal_id")
    private String portalId;

    private boolean enabled;

    @JsonProperty("base_url")
    private String baseUrl;

    @JsonProperty("listing_url")
    private String listingUrl;

    @JsonProperty("user_agent")
    private String userAgent;

    private String notes;

    @JsonProperty("rate_limit_ms")
    private Integer rateLimitMs;

    @JsonProperty("max_retries")
    private Integer maxRetries;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public PortalConfigResponse() {
    }

    public PortalConfigResponse(PortalConfig entity) {
        this.id = entity.getId();
        this.portalId = entity.getPortalId();
        this.enabled = entity.isEnabled();
        this.baseUrl = entity.getBaseUrl();
        this.listingUrl = entity.getListingUrl();
        this.userAgent = entity.getUserAgent();
        this.notes = entity.getNotes();
        this.rateLimitMs = entity.getRateLimitMs();
        this.maxRetries = entity.getMaxRetries();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPortalId() {
        return portalId;
    }

    public void setPortalId(String portalId) {
        this.portalId = portalId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getListingUrl() {
        return listingUrl;
    }

    public void setListingUrl(String listingUrl) {
        this.listingUrl = listingUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getRateLimitMs() {
        return rateLimitMs;
    }

    public void setRateLimitMs(Integer rateLimitMs) {
        this.rateLimitMs = rateLimitMs;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
