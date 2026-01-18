package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    private final MatchingEngine matchingEngine = new MatchingEngine();

    @Test
    void testGenerateCvWithBasicMatch() {
        // Arrange
        CvMaster cvMaster = new CvMaster();
        cvMaster.setName("John Doe");
        cvMaster.setTitle("Senior Software Engineer");
        cvMaster.setEmail("john.doe@example.com");
        cvMaster.setLocation("New York, USA");
        cvMaster.setSummary(List.of("Experienced software engineer with 10 years in backend development"));

        CvMaster.Skill skill1 = new CvMaster.Skill();
        skill1.setName("Java");
        skill1.setLevel("expert");

        CvMaster.Skill skill2 = new CvMaster.Skill();
        skill2.setName("Spring Boot");
        skill2.setLevel("advanced");

        cvMaster.setSkills(List.of(skill1, skill2));
        cvMaster.setDomains(List.of("Finance", "E-commerce"));

        CvMaster.Experience experience = new CvMaster.Experience();
        experience.setCompany("Tech Corp");
        experience.setTitle("Senior Engineer");
        experience.setStart("2020-01");
        experience.setEnd("present");
        experience.setCountry("USA");

        CvMaster.Project project = new CvMaster.Project();
        project.setName("Payment System");
        project.setSituation("Legacy system needed modernization");
        project.setTask("Redesign payment processing");
        project.setActions(List.of("Implemented microservices", "Added caching layer"));
        project.setResult("Reduced processing time by 50%");
        project.setTechStack(List.of("Java", "Spring Boot", "Redis", "PostgreSQL"));
        project.setDomains(List.of("Finance"));

        experience.setProjects(List.of(project));
        cvMaster.setExperiences(List.of(experience));

        CvMaster.Education education = new CvMaster.Education();
        education.setDegree("BSc Computer Science");
        education.setInstitution("University of Technology");
        education.setCountry("USA");
        education.setStart("2010");
        education.setEnd("2014");
        cvMaster.setEducation(List.of(education));

        CvMaster.Language language = new CvMaster.Language();
        language.setName("English");
        language.setLevel("native");
        cvMaster.setLanguages(List.of(language));

        Job job = new Job();
        job.setId("job-123");
        job.setTitle("Senior Backend Engineer");
        job.setLocation("Remote");
        job.setSeniority("senior");
        job.setRawDescription("Looking for an experienced backend engineer");

        Job.Requirements requirements = new Job.Requirements();
        requirements.setYearsOfExperience(5);
        requirements.setMustHaveSkills(List.of("Java", "Spring Boot"));
        requirements.setNiceToHaveSkills(List.of("Redis", "PostgreSQL"));
        requirements.setTools(List.of("Git", "Docker"));
        requirements.setDomains(List.of("Finance"));
        job.setRequirements(requirements);

        job.setResponsibilities(List.of("Design scalable systems", "Mentor junior developers"));
        job.setSoftSkills(List.of("leadership", "communication"));

        Options options = new Options();
        options.setLanguage("en");

        // Act
        CvGenerated result = matchingEngine.generateCv(cvMaster, job, options);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals("job-123", result.getMeta().getJobId());
        assertEquals("Senior Backend Engineer", result.getMeta().getJobTitle());
        assertNotNull(result.getMeta().getMatchingScoreOverall());
        assertTrue(result.getMeta().getMatchingScoreOverall() > 0.0);
        assertTrue(result.getMeta().getMatchingScoreOverall() <= 1.0);

        assertNotNull(result.getMeta().getMatchingDetails());
        assertTrue(result.getMeta().getMatchingDetails().getSkillsCoverage() > 0.0);
        assertTrue(result.getMeta().getMatchingDetails().getDomainFit() > 0.0);
        assertTrue(result.getMeta().getMatchingDetails().getExperienceRelevance() > 0.0);

        assertNotNull(result.getHeader());
        assertEquals("John Doe", result.getHeader().getName());
        assertEquals("Senior Software Engineer", result.getHeader().getTitle());

        assertNotNull(result.getSkillsSection());
        assertNotNull(result.getSkillsSection().getHighlightedSkills());
        assertTrue(result.getSkillsSection().getHighlightedSkills().contains("Java") ||
                   result.getSkillsSection().getHighlightedSkills().contains("java"));

        assertNotNull(result.getExperienceSection());
        assertFalse(result.getExperienceSection().isEmpty());
        assertTrue(result.getExperienceSection().get(0).getRelevanceScore() > 0.0);

        assertNotNull(result.getEducationSection());
        assertFalse(result.getEducationSection().isEmpty());

        assertNotNull(result.getLanguagesSection());
        assertFalse(result.getLanguagesSection().isEmpty());

        assertNotNull(result.getOutput());
        assertNotNull(result.getOutput().getMarkdown());
        assertTrue(result.getOutput().getMarkdown().contains("John Doe"));
    }

    @Test
    void testGenerateCvWithMinimalData() {
        // Arrange
        CvMaster cvMaster = new CvMaster();
        cvMaster.setName("Jane Smith");
        cvMaster.setTitle("Developer");
        cvMaster.setEmail("jane@example.com");
        cvMaster.setSkills(List.of());
        cvMaster.setExperiences(List.of());

        Job job = new Job();
        job.setId("job-456");
        job.setTitle("Junior Developer");
        job.setRequirements(new Job.Requirements());

        Options options = new Options();

        // Act
        CvGenerated result = matchingEngine.generateCv(cvMaster, job, options);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertNotNull(result.getMeta().getMatchingScoreOverall());
        assertTrue(result.getMeta().getMatchingScoreOverall() >= 0.0);
        assertTrue(result.getMeta().getMatchingScoreOverall() <= 1.0);
    }
}
