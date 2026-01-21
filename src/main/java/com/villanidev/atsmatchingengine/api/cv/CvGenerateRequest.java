package com.villanidev.atsmatchingengine.api.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CvGenerateRequest {

    @NotNull
    @Valid
    @JsonProperty("cv_master")
    private CvMaster cvMaster;

    @NotNull
    @Valid
    private Job job;

    private Options options;

    public CvMaster getCvMaster() {
        return cvMaster;
    }

    public void setCvMaster(CvMaster cvMaster) {
        this.cvMaster = cvMaster;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }
}