package com.villanidev.atsmatchingengine.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TextRelevanceStrategy {
    BM25,
    TFIDF,
    NONE;

    @JsonCreator
    public static TextRelevanceStrategy from(String value) {
        if (value == null || value.isBlank()) {
            return BM25;
        }
        return switch (value.trim().toUpperCase()) {
            case "TFIDF", "TF_IDF", "TF-IDF" -> TFIDF;
            case "NONE", "OFF", "DISABLED" -> NONE;
            default -> BM25;
        };
    }
}