package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvGenerated;

public class OutputRenderer {

    public CvGenerated.Output buildOutput(CvGenerated cvGenerated) {
        CvGenerated.Output output = new CvGenerated.Output();

        StringBuilder markdown = new StringBuilder();

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

        if (cvGenerated.getSummary() != null && !cvGenerated.getSummary().isEmpty()) {
            markdown.append("## Summary\n\n");
            cvGenerated.getSummary().forEach(s -> markdown.append(s).append("\n\n"));
        }

        if (cvGenerated.getSkillsSection() != null && cvGenerated.getSkillsSection().getHighlightedSkills() != null) {
            markdown.append("## Skills\n\n");
            markdown.append(String.join(", ", cvGenerated.getSkillsSection().getHighlightedSkills())).append("\n\n");
        }

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
