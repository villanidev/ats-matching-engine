package com.villanidev.atsmatchingengine.templates;

import com.villanidev.atsmatchingengine.domain.CvGenerated;

public class DefaultMarkdownTemplate implements CvTemplate {

    @Override
    public String renderMarkdown(CvGenerated cvGenerated) {
        StringBuilder markdown = new StringBuilder();

        String name = valueOrUnknown(cvGenerated.getHeader().getName(), "Candidate");
        String title = valueOrUnknown(cvGenerated.getHeader().getTitle(), "Professional");

        markdown.append("# ").append(name).append("\n");
        markdown.append("**").append(title).append("**\n\n");

        String contactLine = joinNonBlank(" | ",
                prefixIfPresent("Location", cvGenerated.getHeader().getLocation()),
                prefixIfPresent("Email", cvGenerated.getHeader().getEmail()),
                prefixIfPresent("Phone", cvGenerated.getHeader().getPhone()),
                prefixIfPresent("LinkedIn", cvGenerated.getHeader().getLinkedin())
        );
        if (!contactLine.isBlank()) {
            markdown.append(contactLine).append("\n");
        }
        markdown.append("\n---\n\n");

        if (cvGenerated.getSummary() != null && !cvGenerated.getSummary().isEmpty()) {
            markdown.append("## Summary\n\n");
            cvGenerated.getSummary().forEach(s -> markdown.append("- ").append(s).append("\n"));
            markdown.append("\n");
        }

        if (cvGenerated.getSkillsSection() != null && cvGenerated.getSkillsSection().getHighlightedSkills() != null) {
            markdown.append("## Skills\n\n");
            markdown.append(String.join(" · ", cvGenerated.getSkillsSection().getHighlightedSkills())).append("\n\n");
        }

        if (cvGenerated.getExperienceSection() != null && !cvGenerated.getExperienceSection().isEmpty()) {
            markdown.append("## Experience\n\n");
            for (CvGenerated.ExperienceSection exp : cvGenerated.getExperienceSection()) {
                String role = joinNonBlank(" · ",
                        valueOrUnknown(exp.getTitle(), "Role"),
                        valueOrUnknown(exp.getCompany(), "Company")
                );
                markdown.append("### ").append(role).append("\n");

                String timeline = joinNonBlank(" · ",
                        joinNonBlank(" - ", exp.getStart(), exp.getEnd()),
                        exp.getCountry() != null ? "Location: " + exp.getCountry() : null
                );
                if (!timeline.isBlank()) {
                    markdown.append(timeline).append("\n");
                }
                markdown.append("\n");
                if (exp.getBullets() != null) {
                    exp.getBullets().forEach(bullet -> markdown.append("- ").append(bullet).append("\n"));
                }
                markdown.append("\n");
            }
        }

        if (cvGenerated.getEducationSection() != null && !cvGenerated.getEducationSection().isEmpty()) {
            markdown.append("## Education\n\n");
            for (CvGenerated.EducationSection edu : cvGenerated.getEducationSection()) {
                String degreeLine = valueOrUnknown(edu.getDegree(), "Degree");
                markdown.append("### ").append(degreeLine).append("\n");

                String institutionLine = joinNonBlank(", ", edu.getInstitution(), edu.getCountry());
                if (!institutionLine.isBlank()) {
                    markdown.append(institutionLine).append("\n");
                }

                String period = joinNonBlank(" - ", edu.getStart(), edu.getEnd());
                if (!period.isBlank()) {
                    markdown.append(period).append("\n");
                }

                markdown.append("\n");
            }
        }

        if (cvGenerated.getLanguagesSection() != null && !cvGenerated.getLanguagesSection().isEmpty()) {
            markdown.append("## Languages\n\n");
            for (CvGenerated.LanguageSection lang : cvGenerated.getLanguagesSection()) {
                markdown.append("- ").append(lang.getName()).append(": ").append(lang.getLevel()).append("\n");
            }
        }

        return markdown.toString();
    }

    private String joinNonBlank(String separator, String... values) {
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!joined.isEmpty()) {
                joined.append(separator);
            }
            joined.append(value.trim());
        }
        return joined.toString();
    }

    private String prefixIfPresent(String label, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return label + ": " + value.trim();
    }

    private String valueOrUnknown(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
