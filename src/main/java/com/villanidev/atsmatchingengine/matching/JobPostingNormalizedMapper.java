package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.elt.RequirementsExtractionResult;
import com.villanidev.atsmatchingengine.elt.RequirementsExtractor;
import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JobPostingNormalizedMapper {

    private final RequirementsExtractor requirementsExtractor;

    public JobPostingNormalizedMapper(RequirementsExtractor requirementsExtractor) {
        this.requirementsExtractor = requirementsExtractor;
    }

    public Job toJob(JobPostingNormalized normalized) {
        Job job = new Job();
        job.setId(String.valueOf(normalized.getId()));
        job.setTitle(normalized.getTitle() != null ? normalized.getTitle() : "Unknown");
        job.setLocation(normalized.getLocation());
        job.setRawDescription(normalized.getDescription());
        job.setRequirements(buildRequirements(normalized.getRequirements()));
        return job;
    }

    private Job.Requirements buildRequirements(String requirementsText) {
        Job.Requirements requirements = new Job.Requirements();
        String raw = requirementsText != null ? requirementsText : "";
        List<String> items = Arrays.stream(raw.split("\\r?\\n|,|;"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.replaceAll("^[•\u2022\\-]+\\s*", ""))
                .collect(Collectors.toList());
        RequirementsExtractionResult extracted = requirementsExtractor.extractAll(raw);
        requirements.setMustHaveSkills(extracted.getSkills().isEmpty() ? items : extracted.getSkills());
        requirements.setNiceToHaveSkills(List.of());
        requirements.setTools(extracted.getTools());
        requirements.setDomains(extracted.getDomains());
        requirements.setMethodologies(extracted.getMethodologies());
        return requirements;
    }
}
