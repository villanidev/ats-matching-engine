package com.villanidev.atsmatchingengine.api.elt;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class PortalConfigRequest {

    @NotBlank
    @JsonProperty("portal_id")
    private String portalId;

    private boolean enabled = true;

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
}
