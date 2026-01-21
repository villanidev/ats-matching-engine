package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;

import java.util.*;

public class CvSectionsBuilder {

    private final CandidateSkillCollector skillCollector = new CandidateSkillCollector();
    private final SkillNormalizer skillNormalizer = new SkillNormalizer();

    public CvGenerated.Header buildHeader(CvMaster cvMaster) {
        CvGenerated.Header header = new CvGenerated.Header();
        header.setName(cvMaster.getName());
        header.setTitle(cvMaster.getTitle());
        header.setLocation(cvMaster.getLocation());
        header.setPhone(cvMaster.getPhone());
        header.setEmail(cvMaster.getEmail());
        header.setLinkedin(cvMaster.getLinkedin());
        return header;
    }

    public CvGenerated.SkillsSection buildSkillsSection(CvMaster cvMaster, Job job) {
        CvGenerated.SkillsSection skillsSection = new CvGenerated.SkillsSection();

        Set<String> allSkills = skillCollector.collectSkills(cvMaster);

        Set<String> highlightedSkills = new HashSet<>();
        Job.Requirements requirements = job.getRequirements();

        if (requirements.getMustHaveSkills() != null) {
            requirements.getMustHaveSkills().forEach(required ->
                    allSkills.stream()
                            .filter(candidateSkill -> skillNormalizer.skillMatches(candidateSkill, required))
                            .findFirst()
                            .ifPresent(highlightedSkills::add)
            );
        }

        if (requirements.getNiceToHaveSkills() != null) {
            requirements.getNiceToHaveSkills().forEach(required ->
                    allSkills.stream()
                            .filter(candidateSkill -> skillNormalizer.skillMatches(candidateSkill, required))
                            .findFirst()
                            .ifPresent(highlightedSkills::add)
            );
        }

        if (requirements.getTools() != null) {
            requirements.getTools().forEach(required ->
                    allSkills.stream()
                            .filter(candidateSkill -> skillNormalizer.skillMatches(candidateSkill, required))
                            .findFirst()
                            .ifPresent(highlightedSkills::add)
            );
        }

        skillsSection.setHighlightedSkills(new ArrayList<>(highlightedSkills));

        Map<String, List<String>> skillsGrouped = new HashMap<>();
        skillsGrouped.put("all", new ArrayList<>(allSkills));
        skillsSection.setSkillsGrouped(skillsGrouped);

        return skillsSection;
    }

    public List<CvGenerated.ExperienceSection> buildExperienceSection(CvMaster cvMaster,
                                                                      Job job,
                                                                      Options options,
                                                                      ExperienceScorer experienceScorer) {
        List<CvGenerated.ExperienceSection> experienceSections = new ArrayList<>();

        if (cvMaster.getExperiences() == null) {
            return experienceSections;
        }

        for (CvMaster.Experience exp : cvMaster.getExperiences()) {
            CvGenerated.ExperienceSection section = new CvGenerated.ExperienceSection();
            section.setCompany(exp.getCompany());
            section.setCountry(exp.getCountry());
            section.setTitle(exp.getTitle());
            section.setStart(exp.getStart());
            section.setEnd(exp.getEnd());

            double relevanceScore = experienceScorer.computeExperienceRelevance(exp, job);
            section.setRelevanceScore(relevanceScore);

            List<String> bullets = new ArrayList<>();
            Set<String> techStack = new HashSet<>();

            if (exp.getProjects() != null) {
                for (CvMaster.Project proj : exp.getProjects()) {
                    if (proj.getActions() != null) {
                        bullets.addAll(proj.getActions());
                    }
                    if (proj.getResult() != null && !proj.getResult().isEmpty()) {
                        bullets.add(proj.getResult());
                    }
                    if (proj.getTechStack() != null) {
                        techStack.addAll(proj.getTechStack());
                    }
                }
            }

            section.setBullets(bullets);
            section.setTechStack(new ArrayList<>(techStack));

            experienceSections.add(section);
        }

        experienceSections.sort(Comparator.comparing(CvGenerated.ExperienceSection::getRelevanceScore).reversed());

        applyExperienceOptions(experienceSections, options);

        return experienceSections;
    }

    private void applyExperienceOptions(List<CvGenerated.ExperienceSection> experienceSections, Options options) {
        if (options == null || experienceSections.isEmpty()) {
            return;
        }

        boolean includeOnlyRelevant = Boolean.TRUE.equals(options.getIncludeOnlyRelevantExperiences());
        Double threshold = options.getRelevanceThreshold();
        Integer maxExperiences = options.getMaxExperiences();

        if (includeOnlyRelevant && threshold != null) {
            experienceSections.removeIf(section -> section.getRelevanceScore() == null || section.getRelevanceScore() < threshold);
        }

        if (maxExperiences != null && maxExperiences > 0 && experienceSections.size() > maxExperiences) {
            experienceSections.subList(maxExperiences, experienceSections.size()).clear();
        }
    }

    public List<CvGenerated.EducationSection> buildEducationSection(CvMaster cvMaster) {
        List<CvGenerated.EducationSection> educationSections = new ArrayList<>();

        if (cvMaster.getEducation() == null) {
            return educationSections;
        }

        for (CvMaster.Education edu : cvMaster.getEducation()) {
            CvGenerated.EducationSection section = new CvGenerated.EducationSection();
            section.setDegree(edu.getDegree());
            section.setInstitution(edu.getInstitution());
            section.setCountry(edu.getCountry());
            section.setStart(edu.getStart());
            section.setEnd(edu.getEnd());
            educationSections.add(section);
        }

        return educationSections;
    }

    public List<CvGenerated.LanguageSection> buildLanguagesSection(CvMaster cvMaster) {
        List<CvGenerated.LanguageSection> languageSections = new ArrayList<>();

        if (cvMaster.getLanguages() == null) {
            return languageSections;
        }

        for (CvMaster.Language lang : cvMaster.getLanguages()) {
            CvGenerated.LanguageSection section = new CvGenerated.LanguageSection();
            section.setName(lang.getName());
            section.setLevel(lang.getLevel());
            languageSections.add(section);
        }

        return languageSections;
    }
}
