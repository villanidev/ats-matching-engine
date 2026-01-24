package com.villanidev.atsmatchingengine.api.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedEntity;

public class CvGenerateFromDbResponse {

    @JsonProperty("generated_id")
    private Long generatedId;

    public CvGenerateFromDbResponse() {
    }

    public CvGenerateFromDbResponse(CvGeneratedEntity entity) {
        this.generatedId = entity.getId();
    }

    public Long getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(Long generatedId) {
        this.generatedId = generatedId;
    }
}
