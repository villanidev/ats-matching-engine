package com.villanidev.atsmatchingengine.cv;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.matching.MatchingEngine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CvGenerator {

    private final MatchingEngine matchingEngine;
    private final SummaryGenerator summaryGenerator;
    private final SkillRanker skillRanker;

    public CvGenerator(MatchingEngine matchingEngine,
                       SummaryGenerator summaryGenerator,
                       SkillRanker skillRanker) {
        this.matchingEngine = matchingEngine;
        this.summaryGenerator = summaryGenerator;
        this.skillRanker = skillRanker;
    }

    public CvGenerated generate(CvMaster cvMaster, Job job, Options options) {
        CvGenerated generated = matchingEngine.generateCv(cvMaster, job, options);

        List<String> focusedSummary = summaryGenerator.generateSummary(cvMaster, job);
        generated.setSummary(focusedSummary);

        if (generated.getSkillsSection() != null) {
            List<String> rankedSkills = skillRanker.rankSkills(cvMaster, job);
            generated.getSkillsSection().setHighlightedSkills(rankedSkills);
            if (generated.getSkillsSection().getSkillsGrouped() != null) {
                generated.getSkillsSection().getSkillsGrouped().put("all", rankedSkills);
            }
        }

        return generated;
    }
}
