package com.villanidev.atsmatchingengine.api.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.cv.storage.CvMasterEntity;

public class CvMasterSaveResponse {

    @JsonProperty("cv_master_id")
    private Long cvMasterId;

    public CvMasterSaveResponse() {
    }

    public CvMasterSaveResponse(CvMasterEntity entity) {
        this.cvMasterId = entity.getId();
    }

    public Long getCvMasterId() {
        return cvMasterId;
    }

    public void setCvMasterId(Long cvMasterId) {
        this.cvMasterId = cvMasterId;
    }
}
