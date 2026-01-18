package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingEngine {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public CvGenerated generateCv(CvMaster cvMaster, Job job, Options options) {
        CvGenerated cvGenerated = new CvGenerated();

        // Build meta section with matching scores
        CvGenerated.Meta meta = buildMeta(cvMaster, job);
        cvGenerated.setMeta(meta);

        // Build header section
        CvGenerated.Header header = buildHeader(cvMaster);
        cvGenerated.setHeader(header);

        // Build summary section
        cvGenerated.setSummary(cvMaster.getSummary() != null ? cvMaster.getSummary() : List.of());

        // Build skills section
        CvGenerated.SkillsSection skillsSection = buildSkillsSection(cvMaster, job);
        cvGenerated.setSkillsSection(skillsSection);

        // Build experience section with relevance scores
        List<CvGenerated.ExperienceSection> experienceSection = buildExperienceSection(cvMaster, job, options);
        cvGenerated.setExperienceSection(experienceSection);

        // Build education section
        List<CvGenerated.EducationSection> educationSection = buildEducationSection(cvMaster);
        cvGenerated.setEducationSection(educationSection);

        // Build languages section
        List<CvGenerated.LanguageSection> languagesSection = buildLanguagesSection(cvMaster);
        cvGenerated.setLanguagesSection(languagesSection);

        // Build output section (markdown)
        CvGenerated.Output output = buildOutput(cvGenerated);
        cvGenerated.setOutput(output);

        return cvGenerated;
    }

    private CvGenerated.Meta buildMeta(CvMaster cvMaster, Job job) {
        CvGenerated.Meta meta = new CvGenerated.Meta();
        meta.setJobId(job.getId());
        meta.setJobTitle(job.getTitle());
        meta.setGenerationTimestamp(LocalDate.now().toString());

        // Compute matching scores
        double globalSkillScore = computeGlobalSkillScore(cvMaster, job);
        double domainScore = computeGlobalDomainScore(cvMaster, job);
        double experienceYearsScore = computeExperienceYearsScore(cvMaster, job);
        double softSkillScore = computeSoftSkillScore(cvMaster, job);

        // Compute average of top 3 experience relevance scores
        List<Double> experienceRelevanceScores = cvMaster.getExperiences().stream()
                .map(exp -> computeExperienceRelevance(exp, job))
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .collect(Collectors.toList());

        double avgTopExperiences = experienceRelevanceScores.isEmpty() ? 0.0 :
                experienceRelevanceScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Overall matching score with weights
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
        // Aggregate all candidate skills
        Set<String> candidateSkills = new HashSet<>();

        // Add skills from cv_master.skills
        if (cvMaster.getSkills() != null) {
            cvMaster.getSkills().stream()
                    .map(CvMaster.Skill::getName)
                    .forEach(candidateSkills::add);
        }

        // Add skills from all tech_stacks in experiences
        if (cvMaster.getExperiences() != null) {
            cvMaster.getExperiences().stream()
                    .filter(exp -> exp.getProjects() != null)
                    .flatMap(exp -> exp.getProjects().stream())
                    .filter(proj -> proj.getTechStack() != null)
                    .flatMap(proj -> proj.getTechStack().stream())
                    .forEach(candidateSkills::add);
        }

        Job.Requirements requirements = job.getRequirements();

        // Compute coverage for must_have_skills
        double mustHaveCoverage = computeCoverage(
                candidateSkills,
                requirements.getMustHaveSkills() != null ? requirements.getMustHaveSkills() : List.of()
        );

        // Compute coverage for nice_to_have_skills
        double niceToHaveCoverage = computeCoverage(
                candidateSkills,
                requirements.getNiceToHaveSkills() != null ? requirements.getNiceToHaveSkills() : List.of()
        );

        // Compute coverage for tools
        double toolsCoverage = computeCoverage(
                candidateSkills,
                requirements.getTools() != null ? requirements.getTools() : List.of()
        );

        // Weighted combination
        return 0.6 * mustHaveCoverage + 0.25 * niceToHaveCoverage + 0.15 * toolsCoverage;
    }

    private double computeCoverage(Set<String> candidateSkills, List<String> requiredSkills) {
        if (requiredSkills.isEmpty()) {
            return 1.0; // No requirements means full coverage
        }

        long matchedCount = requiredSkills.stream()
                .filter(required -> skillMatches(candidateSkills, required))
                .count();

        return (double) matchedCount / requiredSkills.size();
    }

    private boolean skillMatches(Set<String> candidateSkills, String requiredSkill) {
        String normalizedRequired = normalizeSkill(requiredSkill);
        return candidateSkills.stream()
                .anyMatch(candidate -> normalizeSkill(candidate).equals(normalizedRequired));
    }

    private String normalizeSkill(String skill) {
        return skill.trim().toLowerCase();
    }

    private double computeGlobalDomainScore(CvMaster cvMaster, Job job) {
        Job.Requirements requirements = job.getRequirements();
        List<String> jobDomains = requirements.getDomains();

        if (jobDomains == null || jobDomains.isEmpty()) {
            return 0.5; // Neutral value if no domains specified
        }

        List<String> candidateDomains = cvMaster.getDomains() != null ? cvMaster.getDomains() : List.of();

        if (candidateDomains.isEmpty()) {
            return 0.0;
        }

        long matchedCount = jobDomains.stream()
                .filter(jobDomain -> candidateDomains.stream()
                        .anyMatch(candidateDomain -> normalizeSkill(candidateDomain).equals(normalizeSkill(jobDomain))))
                .count();

        return (double) matchedCount / jobDomains.size();
    }

    private double computeExperienceYearsScore(CvMaster cvMaster, Job job) {
        Job.Requirements requirements = job.getRequirements();
        Integer requiredYears = requirements.getYearsOfExperience();

        if (requiredYears == null || requiredYears == 0) {
            return 1.0; // No requirement means full score
        }

        // Calculate candidate's years of experience
        double candidateYears = calculateTotalYearsOfExperience(cvMaster);

        if (candidateYears >= requiredYears) {
            return 1.0;
        } else {
            return candidateYears / requiredYears;
        }
    }

    private double calculateTotalYearsOfExperience(CvMaster cvMaster) {
        if (cvMaster.getExperiences() == null || cvMaster.getExperiences().isEmpty()) {
            return 0.0;
        }

        // Find earliest start date
        Optional<YearMonth> earliestStart = cvMaster.getExperiences().stream()
                .map(CvMaster.Experience::getStart)
                .filter(Objects::nonNull)
                .map(this::parseYearMonth)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());

        if (earliestStart.isEmpty()) {
            return 0.0;
        }

        YearMonth now = YearMonth.now();
        return earliestStart.get().until(now, ChronoUnit.MONTHS) / 12.0;
    }

    private Optional<YearMonth> parseYearMonth(String dateStr) {
        try {
            return Optional.of(YearMonth.parse(dateStr, YEAR_MONTH_FORMATTER));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private double computeSoftSkillScore(CvMaster cvMaster, Job job) {
        List<String> softSkills = job.getSoftSkills();

        if (softSkills == null || softSkills.isEmpty()) {
            return 0.5; // Neutral value if no soft skills specified
        }

        // Concatenate candidate text
        StringBuilder candidateText = new StringBuilder();

        // Add summary
        if (cvMaster.getSummary() != null) {
            cvMaster.getSummary().forEach(s -> candidateText.append(s).append(" "));
        }

        // Add experience text fields
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

        // Count how many soft skills appear as substrings
        long matchedCount = softSkills.stream()
                .filter(skill -> candidateTextLower.contains(skill.toLowerCase()))
                .count();

        return (double) matchedCount / softSkills.size();
    }

    private double computeExperienceRelevance(CvMaster.Experience experience, Job job) {
        // Compute skill score for this experience
        Set<String> experienceSkills = new HashSet<>();
        if (experience.getProjects() != null) {
            experience.getProjects().stream()
                    .filter(proj -> proj.getTechStack() != null)
                    .flatMap(proj -> proj.getTechStack().stream())
                    .forEach(experienceSkills::add);
        }

        Job.Requirements requirements = job.getRequirements();

        // Aggregate all job skills
        List<String> allJobSkills = new ArrayList<>();
        if (requirements.getMustHaveSkills() != null) allJobSkills.addAll(requirements.getMustHaveSkills());
        if (requirements.getNiceToHaveSkills() != null) allJobSkills.addAll(requirements.getNiceToHaveSkills());
        if (requirements.getTools() != null) allJobSkills.addAll(requirements.getTools());

        double skillScore = computeCoverage(experienceSkills, allJobSkills);

        // Compute domain score for this experience
        Set<String> experienceDomains = new HashSet<>();
        if (experience.getProjects() != null) {
            experience.getProjects().stream()
                    .filter(proj -> proj.getDomains() != null)
                    .flatMap(proj -> proj.getDomains().stream())
                    .forEach(experienceDomains::add);
        }

        List<String> jobDomains = requirements.getDomains() != null ? requirements.getDomains() : List.of();
        double domainScore;
        if (jobDomains.isEmpty()) {
            domainScore = 0.5;
        } else if (experienceDomains.isEmpty()) {
            domainScore = 0.0;
        } else {
            long matchedDomains = jobDomains.stream()
                    .filter(jobDomain -> experienceDomains.stream()
                            .anyMatch(expDomain -> normalizeSkill(expDomain).equals(normalizeSkill(jobDomain))))
                    .count();
            domainScore = (double) matchedDomains / jobDomains.size();
        }

        // Compute recency score
        double recencyScore = computeRecencyScore(experience.getEnd());

        // Combine with weights
        return 0.6 * skillScore + 0.2 * domainScore + 0.2 * recencyScore;
    }

    private double computeRecencyScore(String endDate) {
        if ("present".equalsIgnoreCase(endDate)) {
            return 1.0; // Current job
        }

        Optional<YearMonth> endYearMonth = parseYearMonth(endDate);
        if (endYearMonth.isEmpty()) {
            return 0.5; // Default if can't parse
        }

        YearMonth now = YearMonth.now();
        long monthsAgo = endYearMonth.get().until(now, ChronoUnit.MONTHS);
        double yearsAgo = monthsAgo / 12.0;

        if (yearsAgo <= 0) {
            return 1.0;
        } else if (yearsAgo >= 5) {
            return 0.2;
        } else {
            // Linear decay from 1.0 to 0.2 over 5 years
            return 1.0 - (yearsAgo / 5.0) * 0.8;
        }
    }

    private CvGenerated.Header buildHeader(CvMaster cvMaster) {
        CvGenerated.Header header = new CvGenerated.Header();
        header.setName(cvMaster.getName());
        header.setTitle(cvMaster.getTitle());
        header.setLocation(cvMaster.getLocation());
        header.setPhone(cvMaster.getPhone());
        header.setEmail(cvMaster.getEmail());
        header.setLinkedin(cvMaster.getLinkedin());
        return header;
    }

    private CvGenerated.SkillsSection buildSkillsSection(CvMaster cvMaster, Job job) {
        CvGenerated.SkillsSection skillsSection = new CvGenerated.SkillsSection();

        // Collect all candidate skills
        Set<String> allSkills = new HashSet<>();
        if (cvMaster.getSkills() != null) {
            cvMaster.getSkills().stream()
                    .map(CvMaster.Skill::getName)
                    .forEach(allSkills::add);
        }

        if (cvMaster.getExperiences() != null) {
            cvMaster.getExperiences().stream()
                    .filter(exp -> exp.getProjects() != null)
                    .flatMap(exp -> exp.getProjects().stream())
                    .filter(proj -> proj.getTechStack() != null)
                    .flatMap(proj -> proj.getTechStack().stream())
                    .forEach(allSkills::add);
        }

        // Highlight skills that match job requirements
        Set<String> highlightedSkills = new HashSet<>();
        Job.Requirements requirements = job.getRequirements();

        if (requirements.getMustHaveSkills() != null) {
            requirements.getMustHaveSkills().stream()
                    .filter(required -> skillMatches(allSkills, required))
                    .forEach(highlightedSkills::add);
        }

        if (requirements.getNiceToHaveSkills() != null) {
            requirements.getNiceToHaveSkills().stream()
                    .filter(required -> skillMatches(allSkills, required))
                    .forEach(highlightedSkills::add);
        }

        skillsSection.setHighlightedSkills(new ArrayList<>(highlightedSkills));

        // Simple grouping - put all skills in "all" group
        Map<String, List<String>> skillsGrouped = new HashMap<>();
        skillsGrouped.put("all", new ArrayList<>(allSkills));
        skillsSection.setSkillsGrouped(skillsGrouped);

        return skillsSection;
    }

    private List<CvGenerated.ExperienceSection> buildExperienceSection(CvMaster cvMaster, Job job, Options options) {
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

            // Compute relevance score
            double relevanceScore = computeExperienceRelevance(exp, job);
            section.setRelevanceScore(relevanceScore);

            // Build bullets from projects
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

        // Sort by relevance score (descending)
        experienceSections.sort(Comparator.comparing(CvGenerated.ExperienceSection::getRelevanceScore).reversed());

        return experienceSections;
    }

    private List<CvGenerated.EducationSection> buildEducationSection(CvMaster cvMaster) {
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

    private List<CvGenerated.LanguageSection> buildLanguagesSection(CvMaster cvMaster) {
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

    private CvGenerated.Output buildOutput(CvGenerated cvGenerated) {
        CvGenerated.Output output = new CvGenerated.Output();

        // Simple markdown generation
        StringBuilder markdown = new StringBuilder();

        // Header
        markdown.append("# ").append(cvGenerated.getHeader().getName()).append("\n\n");
        markdown.append("**").append(cvGenerated.getHeader().getTitle()).append("**\n\n");
        markdown.append("Location: ").append(cvGenerated.getHeader().getLocation()).append("\n");
        markdown.append("Email: ").append(cvGenerated.getHeader().getEmail()).append("\n");
        if (cvGenerated.getHeader().getPhone() != null) {
            markdown.append("Phone: ").append(cvGenerated.getHeader().getPhone()).append("\n");
        }
        if (cvGenerated.getHeader().getLinkedin() != null) {
            markdown.append("LinkedIn: ").append(cvGenerated.getHeader().getLinkedin()).append("\n");
        }
        markdown.append("\n");

        // Summary
        if (cvGenerated.getSummary() != null && !cvGenerated.getSummary().isEmpty()) {
            markdown.append("## Summary\n\n");
            cvGenerated.getSummary().forEach(s -> markdown.append(s).append("\n\n"));
        }

        // Skills
        if (cvGenerated.getSkillsSection() != null && cvGenerated.getSkillsSection().getHighlightedSkills() != null) {
            markdown.append("## Skills\n\n");
            markdown.append(String.join(", ", cvGenerated.getSkillsSection().getHighlightedSkills())).append("\n\n");
        }

        // Experience
        if (cvGenerated.getExperienceSection() != null && !cvGenerated.getExperienceSection().isEmpty()) {
            markdown.append("## Experience\n\n");
            for (CvGenerated.ExperienceSection exp : cvGenerated.getExperienceSection()) {
                markdown.append("### ").append(exp.getTitle()).append(" at ").append(exp.getCompany()).append("\n");
                markdown.append(exp.getStart()).append(" - ").append(exp.getEnd()).append("\n");
                if (exp.getCountry() != null) {
                    markdown.append("Location: ").append(exp.getCountry()).append("\n");
                }
                markdown.append("\n");
                if (exp.getBullets() != null) {
                    exp.getBullets().forEach(bullet -> markdown.append("- ").append(bullet).append("\n"));
                }
                markdown.append("\n");
            }
        }

        // Education
        if (cvGenerated.getEducationSection() != null && !cvGenerated.getEducationSection().isEmpty()) {
            markdown.append("## Education\n\n");
            for (CvGenerated.EducationSection edu : cvGenerated.getEducationSection()) {
                markdown.append("### ").append(edu.getDegree()).append("\n");
                markdown.append(edu.getInstitution());
                if (edu.getCountry() != null) {
                    markdown.append(", ").append(edu.getCountry());
                }
                markdown.append("\n");
                if (edu.getStart() != null && edu.getEnd() != null) {
                    markdown.append(edu.getStart()).append(" - ").append(edu.getEnd()).append("\n");
                }
                markdown.append("\n");
            }
        }

        // Languages
        if (cvGenerated.getLanguagesSection() != null && !cvGenerated.getLanguagesSection().isEmpty()) {
            markdown.append("## Languages\n\n");
            for (CvGenerated.LanguageSection lang : cvGenerated.getLanguagesSection()) {
                markdown.append("- ").append(lang.getName()).append(": ").append(lang.getLevel()).append("\n");
            }
        }

        output.setMarkdown(markdown.toString());

        return output;
    }
}
