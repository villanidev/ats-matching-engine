package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JobPostingNormalizedMapper {

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
        if (requirementsText == null || requirementsText.isBlank()) {
            requirements.setMustHaveSkills(List.of());
            requirements.setNiceToHaveSkills(List.of());
            requirements.setTools(List.of());
            requirements.setDomains(List.of());
            return requirements;
        }
        List<String> items = Arrays.stream(requirementsText.split("\\r?\\n|,|;"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.replaceAll("^[•\u2022\\-]+\\s*", ""))
                .collect(Collectors.toList());
        requirements.setMustHaveSkills(items);
        requirements.setNiceToHaveSkills(List.of());
        requirements.setTools(List.of());
        requirements.setDomains(List.of());
        return requirements;
    }
}
