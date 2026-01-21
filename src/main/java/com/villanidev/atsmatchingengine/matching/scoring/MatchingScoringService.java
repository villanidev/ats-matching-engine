package com.villanidev.atsmatchingengine.matching.scoring;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.shared.CandidateSkillCollector;

import java.time.LocalDate;
import java.util.Comparator;

public class MatchingScoringService {

    private final CandidateSkillCollector skillCollector = new CandidateSkillCollector();
    private final SkillCoverageCalculator coverageCalculator = new SkillCoverageCalculator();
    private final ExperienceScorer experienceScorer = new ExperienceScorer(coverageCalculator);
    private final ExperienceYearsCalculator experienceYearsCalculator = new ExperienceYearsCalculator();
    private final DomainScorer domainScorer = new DomainScorer();
    private final SoftSkillScorer softSkillScorer = new SoftSkillScorer();

    public CvGenerated.Meta buildMeta(CvMaster cvMaster, Job job) {
        CvGenerated.Meta meta = new CvGenerated.Meta();
        meta.setJobId(job.getId());
        meta.setJobTitle(job.getTitle());
        meta.setGenerationTimestamp(LocalDate.now().toString());

        double globalSkillScore = computeGlobalSkillScore(cvMaster, job);
        double domainScore = domainScorer.computeGlobalDomainScore(cvMaster, job);
        double experienceYearsScore = computeExperienceYearsScore(cvMaster, job);
        double softSkillScore = softSkillScorer.computeSoftSkillScore(cvMaster, job);

        double avgTopExperiences = cvMaster.getExperiences().stream()
                .map(exp -> experienceScorer.computeExperienceRelevance(exp, job))
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double overallScore = 0.45 * globalSkillScore +
                0.20 * avgTopExperiences +
                0.10 * domainScore +
                0.10 * experienceYearsScore +
                0.15 * softSkillScore;

        meta.setMatchingScoreOverall(overallScore);

        CvGenerated.MatchingDetails matchingDetails = new CvGenerated.MatchingDetails();
        matchingDetails.setSkillsCoverage(globalSkillScore);
        matchingDetails.setDomainFit(domainScore);
        matchingDetails.setExperienceRelevance(avgTopExperiences);
        meta.setMatchingDetails(matchingDetails);

        return meta;
    }

    private double computeGlobalSkillScore(CvMaster cvMaster, Job job) {
        var candidateSkills = skillCollector.collectSkills(cvMaster);
        Job.Requirements requirements = job.getRequirements();

        double mustHaveCoverage = coverageCalculator.computeCoverage(
                candidateSkills,
                requirements.getMustHaveSkills() != null ? requirements.getMustHaveSkills() : java.util.List.of()
        );

        double niceToHaveCoverage = coverageCalculator.computeCoverage(
                candidateSkills,
                requirements.getNiceToHaveSkills() != null ? requirements.getNiceToHaveSkills() : java.util.List.of()
        );

        double toolsCoverage = coverageCalculator.computeCoverage(
                candidateSkills,
                requirements.getTools() != null ? requirements.getTools() : java.util.List.of()
        );

        return 0.6 * mustHaveCoverage + 0.25 * niceToHaveCoverage + 0.15 * toolsCoverage;
    }

    private double computeExperienceYearsScore(CvMaster cvMaster, Job job) {
        Job.Requirements requirements = job.getRequirements();
        Integer requiredYears = requirements.getYearsOfExperience();

        if (requiredYears == null || requiredYears == 0) {
            return 1.0;
        }

        double candidateYears = experienceYearsCalculator.calculateTotalYearsOfExperience(cvMaster);

        if (candidateYears >= requiredYears) {
            return 1.0;
        } else {
            return candidateYears / requiredYears;
        }
    }

    public ExperienceScorer getExperienceScorer() {
        return experienceScorer;
    }
}