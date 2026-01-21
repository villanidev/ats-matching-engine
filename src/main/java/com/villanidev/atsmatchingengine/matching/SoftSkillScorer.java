package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;

import java.util.List;

public class SoftSkillScorer {

    public double computeSoftSkillScore(CvMaster cvMaster, Job job) {
        List<String> softSkills = job.getSoftSkills();

        if (softSkills == null || softSkills.isEmpty()) {
            return 0.5;
        }

        StringBuilder candidateText = new StringBuilder();

        if (cvMaster.getSummary() != null) {
            cvMaster.getSummary().forEach(s -> candidateText.append(s).append(" "));
        }

        if (cvMaster.getExperiences() != null) {
            cvMaster.getExperiences().stream()
                    .filter(exp -> exp.getProjects() != null)
                    .flatMap(exp -> exp.getProjects().stream())
                    .forEach(proj -> {
                        if (proj.getSituation() != null) candidateText.append(proj.getSituation()).append(" ");
                        if (proj.getTask() != null) candidateText.append(proj.getTask()).append(" ");
                        if (proj.getResult() != null) candidateText.append(proj.getResult()).append(" ");
                        if (proj.getActions() != null) {
                            proj.getActions().forEach(action -> candidateText.append(action).append(" "));
                        }
                    });
        }

        String candidateTextLower = candidateText.toString().toLowerCase();

        long matchedCount = softSkills.stream()
                .filter(skill -> candidateTextLower.contains(skill.toLowerCase()))
                .count();

        return (double) matchedCount / softSkills.size();
    }
}
