package com.villanidev.atsmatchingengine.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.domain.CvGenerated;

public class CvGenerateResponse {
    
    @JsonProperty("cv_generated")
    private CvGenerated cvGenerated;

    public CvGenerateResponse() {
    }

    public CvGenerateResponse(CvGenerated cvGenerated) {
        this.cvGenerated = cvGenerated;
    }

    // Getters and Setters
    public CvGenerated getCvGenerated() {
        return cvGenerated;
    }

    public void setCvGenerated(CvGenerated cvGenerated) {
        this.cvGenerated = cvGenerated;
    }
}
