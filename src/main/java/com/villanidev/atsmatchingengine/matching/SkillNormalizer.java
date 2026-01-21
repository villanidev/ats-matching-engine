package com.villanidev.atsmatchingengine.matching;

import java.util.Set;

public class SkillNormalizer {

    public String normalizeSkill(String skill) {
        if (skill == null) {
            return "";
        }
        return skill.trim().toLowerCase();
    }

    public boolean skillMatches(Set<String> candidateSkills, String requiredSkill) {
        String normalizedRequired = normalizeSkill(requiredSkill);
        return candidateSkills.stream()
                .anyMatch(candidate -> normalizeSkill(candidate).equals(normalizedRequired));
    }

    public boolean skillMatches(String candidateSkill, String requiredSkill) {
        return normalizeSkill(candidateSkill).equals(normalizeSkill(requiredSkill));
    }
}
