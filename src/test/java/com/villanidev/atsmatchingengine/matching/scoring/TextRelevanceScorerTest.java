package com.villanidev.atsmatchingengine.matching.scoring;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.TextRelevanceStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TextRelevanceScorerTest {

    @Test
    void bm25ScoresRelevantText() {
        TextRelevanceScorer scorer = new TextRelevanceScorer();
        CvMaster cvMaster = sampleCv();
        Job job = sampleJob();

        double score = scorer.computeTextRelevanceScore(cvMaster, job, TextRelevanceStrategy.BM25);

        assertTrue(score > 0.0, "BM25 score should be positive");
    }

    @Test
    void tfidfScoresRelevantText() {
        TextRelevanceScorer scorer = new TextRelevanceScorer();
        CvMaster cvMaster = sampleCv();
        Job job = sampleJob();

        double score = scorer.computeTextRelevanceScore(cvMaster, job, TextRelevanceStrategy.TFIDF);

        assertTrue(score > 0.0, "TF-IDF score should be positive");
    }

    private CvMaster sampleCv() {
        CvMaster cvMaster = new CvMaster();
        cvMaster.setName("Jane Doe");
        cvMaster.setEmail("jane@dev.com");
        cvMaster.setSummary(List.of("Backend engineer with Java and Spring Boot experience"));

        CvMaster.Skill skill = new CvMaster.Skill();
        skill.setName("Java");
        cvMaster.setSkills(List.of(skill));
        return cvMaster;
    }

    private Job sampleJob() {
        Job job = new Job();
        job.setId("job-1");
        job.setTitle("Backend Engineer");
        job.setRawDescription("Looking for a Java engineer with Spring Boot experience.");

        Job.Requirements requirements = new Job.Requirements();
        requirements.setMustHaveSkills(List.of("Java", "Spring Boot"));
        job.setRequirements(requirements);
        return job;
    }
}