package com.villanidev.atsmatchingengine.domain;

import java.util.List;

public class Options {
    
    private String language; // en | pt
    
    @com.fasterxml.jackson.annotation.JsonProperty("max_experiences")
    private Integer maxExperiences;
    
    @com.fasterxml.jackson.annotation.JsonProperty("include_only_relevant_experiences")
    private Boolean includeOnlyRelevantExperiences;
    
    @com.fasterxml.jackson.annotation.JsonProperty("relevance_threshold")
    private Double relevanceThreshold;
    
    @com.fasterxml.jackson.annotation.JsonProperty("output_formats")
    private List<String> outputFormats; // structured, markdown
    
    @com.fasterxml.jackson.annotation.JsonProperty("section_order")
    private List<String> sectionOrder; // summary, skills, experience, education, certifications, languages
    
    private String profile; // software_engineer_senior

    @com.fasterxml.jackson.annotation.JsonProperty("text_relevance_strategy")
    private TextRelevanceStrategy textRelevanceStrategy;

    // Getters and Setters
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getMaxExperiences() {
        return maxExperiences;
    }

    public void setMaxExperiences(Integer maxExperiences) {
        this.maxExperiences = maxExperiences;
    }

    public Boolean getIncludeOnlyRelevantExperiences() {
        return includeOnlyRelevantExperiences;
    }

    public void setIncludeOnlyRelevantExperiences(Boolean includeOnlyRelevantExperiences) {
        this.includeOnlyRelevantExperiences = includeOnlyRelevantExperiences;
    }

    public Double getRelevanceThreshold() {
        return relevanceThreshold;
    }

    public void setRelevanceThreshold(Double relevanceThreshold) {
        this.relevanceThreshold = relevanceThreshold;
    }

    public List<String> getOutputFormats() {
        return outputFormats;
    }

    public void setOutputFormats(List<String> outputFormats) {
        this.outputFormats = outputFormats;
    }

    public List<String> getSectionOrder() {
        return sectionOrder;
    }

    public void setSectionOrder(List<String> sectionOrder) {
        this.sectionOrder = sectionOrder;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public TextRelevanceStrategy getTextRelevanceStrategy() {
        return textRelevanceStrategy;
    }

    public void setTextRelevanceStrategy(TextRelevanceStrategy textRelevanceStrategy) {
        this.textRelevanceStrategy = textRelevanceStrategy;
    }
}
