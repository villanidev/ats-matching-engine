package com.villanidev.atsmatchingengine.shared;

import com.villanidev.atsmatchingengine.domain.CvMaster;

import java.util.HashSet;
import java.util.Set;

public class CandidateSkillCollector {

    public Set<String> collectSkills(CvMaster cvMaster) {
        Set<String> candidateSkills = new HashSet<>();

        if (cvMaster.getSkills() != null) {
            cvMaster.getSkills().stream()
                    .map(CvMaster.Skill::getName)
                    .forEach(candidateSkills::add);
        }

        if (cvMaster.getExperiences() != null) {
            cvMaster.getExperiences().stream()
                    .filter(exp -> exp.getProjects() != null)
                    .flatMap(exp -> exp.getProjects().stream())
                    .filter(proj -> proj.getTechStack() != null)
                    .flatMap(proj -> proj.getTechStack().stream())
                    .forEach(candidateSkills::add);
        }

        return candidateSkills;
    }
}
