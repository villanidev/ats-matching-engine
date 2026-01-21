package com.villanidev.atsmatchingengine.cv;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.shared.CandidateSkillCollector;
import com.villanidev.atsmatchingengine.shared.SkillNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SummaryGenerator {

    private final CandidateSkillCollector skillCollector = new CandidateSkillCollector();
    private final SkillNormalizer skillNormalizer = new SkillNormalizer();

    public List<String> generateSummary(CvMaster cvMaster, Job job) {
        List<String> summary = cvMaster.getSummary() != null ? new ArrayList<>(cvMaster.getSummary()) : new ArrayList<>();

        List<String> jobSkills = new ArrayList<>();
        if (job.getRequirements() != null) {
            if (job.getRequirements().getMustHaveSkills() != null) {
                jobSkills.addAll(job.getRequirements().getMustHaveSkills());
            }
            if (job.getRequirements().getNiceToHaveSkills() != null) {
                jobSkills.addAll(job.getRequirements().getNiceToHaveSkills());
            }
            if (job.getRequirements().getTools() != null) {
                jobSkills.addAll(job.getRequirements().getTools());
            }
        }

        Set<String> candidateSkills = skillCollector.collectSkills(cvMaster);
        List<String> matched = new ArrayList<>();
        for (String required : jobSkills) {
            candidateSkills.stream()
                    .filter(candidate -> skillNormalizer.skillMatches(candidate, required))
                    .findFirst()
                    .ifPresent(matched::add);
        }

        if (!matched.isEmpty()) {
            summary.add("Key skills: " + String.join(", ", matched));
        }

        return summary;
    }
}
