package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.shared.SkillNormalizer;

import java.util.List;
import java.util.Set;

public class SkillCoverageCalculator {

    private final SkillNormalizer skillNormalizer = new SkillNormalizer();

    public double computeCoverage(Set<String> candidateSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 1.0;
        }

        long matchedCount = requiredSkills.stream()
                .filter(required -> skillNormalizer.skillMatches(candidateSkills, required))
                .count();

        return (double) matchedCount / requiredSkills.size();
    }
}
