package com.villanidev.atsmatchingengine.api.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.domain.Options;
import jakarta.validation.constraints.NotNull;

public class CvBatchMatchRequest {

    @NotNull
    @JsonProperty("cv_master_id")
    private Long cvMasterId;

    private Options options;

    private Integer limit;

    public Long getCvMasterId() {
        return cvMasterId;
    }

    public void setCvMasterId(Long cvMasterId) {
        this.cvMasterId = cvMasterId;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
