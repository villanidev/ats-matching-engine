package com.villanidev.atsmatchingengine.api.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.domain.Options;
import jakarta.validation.constraints.NotNull;

public class CvGenerateFromDbRequest {

    @NotNull
    @JsonProperty("cv_master_id")
    private Long cvMasterId;

    @NotNull
    @JsonProperty("job_posting_id")
    private Long jobPostingId;

    private Options options;

    public Long getCvMasterId() {
        return cvMasterId;
    }

    public void setCvMasterId(Long cvMasterId) {
        this.cvMasterId = cvMasterId;
    }

    public Long getJobPostingId() {
        return jobPostingId;
    }

    public void setJobPostingId(Long jobPostingId) {
        this.jobPostingId = jobPostingId;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }
}
