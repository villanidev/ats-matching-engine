package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;

import java.util.List;

public class DomainScorer {

    private final SkillNormalizer skillNormalizer = new SkillNormalizer();

    public double computeGlobalDomainScore(CvMaster cvMaster, Job job) {
        Job.Requirements requirements = job.getRequirements();
        List<String> jobDomains = requirements.getDomains();

        if (jobDomains == null || jobDomains.isEmpty()) {
            return 0.5;
        }

        List<String> candidateDomains = cvMaster.getDomains() != null ? cvMaster.getDomains() : List.of();

        if (candidateDomains.isEmpty()) {
            return 0.0;
        }

        long matchedCount = jobDomains.stream()
                .filter(jobDomain -> candidateDomains.stream()
                        .anyMatch(candidateDomain -> skillNormalizer.normalizeSkill(candidateDomain)
                                .equals(skillNormalizer.normalizeSkill(jobDomain))))
                .count();

        return (double) matchedCount / jobDomains.size();
    }
}
