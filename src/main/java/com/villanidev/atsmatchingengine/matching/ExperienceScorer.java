package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.shared.DateParser;
import com.villanidev.atsmatchingengine.shared.SkillNormalizer;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ExperienceScorer {

    private final SkillCoverageCalculator coverageCalculator;
    private final SkillNormalizer skillNormalizer = new SkillNormalizer();
    private final DateParser dateParser = new DateParser();

    public ExperienceScorer(SkillCoverageCalculator coverageCalculator) {
        this.coverageCalculator = coverageCalculator;
    }

    public double computeExperienceRelevance(CvMaster.Experience experience, Job job) {
        Set<String> experienceSkills = new HashSet<>();
        if (experience.getProjects() != null) {
            experience.getProjects().stream()
                    .filter(proj -> proj.getTechStack() != null)
                    .flatMap(proj -> proj.getTechStack().stream())
                    .forEach(experienceSkills::add);
        }

        Job.Requirements requirements = job.getRequirements();

        List<String> allJobSkills = new ArrayList<>();
        if (requirements.getMustHaveSkills() != null) allJobSkills.addAll(requirements.getMustHaveSkills());
        if (requirements.getNiceToHaveSkills() != null) allJobSkills.addAll(requirements.getNiceToHaveSkills());
        if (requirements.getTools() != null) allJobSkills.addAll(requirements.getTools());

        double skillScore = coverageCalculator.computeCoverage(experienceSkills, allJobSkills);

        Set<String> experienceDomains = new HashSet<>();
        if (experience.getProjects() != null) {
            experience.getProjects().stream()
                    .filter(proj -> proj.getDomains() != null)
                    .flatMap(proj -> proj.getDomains().stream())
                    .forEach(experienceDomains::add);
        }

        List<String> jobDomains = requirements.getDomains() != null ? requirements.getDomains() : List.of();
        double domainScore;
        if (jobDomains.isEmpty()) {
            domainScore = 0.5;
        } else if (experienceDomains.isEmpty()) {
            domainScore = 0.0;
        } else {
            long matchedDomains = jobDomains.stream()
                    .filter(jobDomain -> experienceDomains.stream()
                            .anyMatch(expDomain -> skillNormalizer.normalizeSkill(expDomain)
                                    .equals(skillNormalizer.normalizeSkill(jobDomain))))
                    .count();
            domainScore = (double) matchedDomains / jobDomains.size();
        }

        double recencyScore = computeRecencyScore(experience.getEnd());

        return 0.6 * skillScore + 0.2 * domainScore + 0.2 * recencyScore;
    }

    private double computeRecencyScore(String endDate) {
        if ("present".equalsIgnoreCase(endDate)) {
            return 1.0;
        }

        Optional<YearMonth> endYearMonth = dateParser.parseYearMonth(endDate);
        if (endYearMonth.isEmpty()) {
            return 0.5;
        }

        YearMonth now = YearMonth.now();
        long monthsAgo = endYearMonth.get().until(now, ChronoUnit.MONTHS);
        double yearsAgo = monthsAgo / 12.0;

        if (yearsAgo <= 0) {
            return 1.0;
        } else if (yearsAgo >= 5) {
            return 0.2;
        } else {
            return 1.0 - (yearsAgo / 5.0) * 0.8;
        }
    }
}
