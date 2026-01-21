package com.villanidev.atsmatchingengine.cv;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.shared.CandidateSkillCollector;
import com.villanidev.atsmatchingengine.shared.SkillNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SkillRanker {

    private final CandidateSkillCollector skillCollector = new CandidateSkillCollector();
    private final SkillNormalizer skillNormalizer = new SkillNormalizer();

    public List<String> rankSkills(CvMaster cvMaster, Job job) {
        Set<String> candidateSkills = skillCollector.collectSkills(cvMaster);
        Map<String, Double> scores = new HashMap<>();

        List<String> mustHave = job.getRequirements() != null && job.getRequirements().getMustHaveSkills() != null
                ? job.getRequirements().getMustHaveSkills() : List.of();
        List<String> niceToHave = job.getRequirements() != null && job.getRequirements().getNiceToHaveSkills() != null
                ? job.getRequirements().getNiceToHaveSkills() : List.of();
        List<String> tools = job.getRequirements() != null && job.getRequirements().getTools() != null
                ? job.getRequirements().getTools() : List.of();

        for (String skill : candidateSkills) {
            double score = 0.0;
            if (mustHave.stream().anyMatch(required -> skillNormalizer.skillMatches(skill, required))) {
                score = 2.0;
            } else if (niceToHave.stream().anyMatch(required -> skillNormalizer.skillMatches(skill, required))) {
                score = 1.0;
            } else if (tools.stream().anyMatch(required -> skillNormalizer.skillMatches(skill, required))) {
                score = 0.5;
            }
            scores.put(skill, score);
        }

        List<String> ranked = new ArrayList<>(candidateSkills);
        ranked.sort(Comparator
                .comparingDouble((String skill) -> scores.getOrDefault(skill, 0.0)).reversed()
                .thenComparing(String::compareToIgnoreCase));

        return ranked;
    }
}
