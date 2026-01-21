package com.villanidev.atsmatchingengine.domain;

import java.util.List;
import java.util.Map;

public class CvGenerated {
    
    private Meta meta;
    private Header header;
    private List<String> summary;
    
    @com.fasterxml.jackson.annotation.JsonProperty("skills_section")
    private SkillsSection skillsSection;
    
    @com.fasterxml.jackson.annotation.JsonProperty("experience_section")
    private List<ExperienceSection> experienceSection;
    
    @com.fasterxml.jackson.annotation.JsonProperty("education_section")
    private List<EducationSection> educationSection;
    
    @com.fasterxml.jackson.annotation.JsonProperty("languages_section")
    private List<LanguageSection> languagesSection;
    
    private Output output;

    // Getters and Setters
    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<String> getSummary() {
        return summary;
    }

    public void setSummary(List<String> summary) {
        this.summary = summary;
    }

    public SkillsSection getSkillsSection() {
        return skillsSection;
    }

    public void setSkillsSection(SkillsSection skillsSection) {
        this.skillsSection = skillsSection;
    }

    public List<ExperienceSection> getExperienceSection() {
        return experienceSection;
    }

    public void setExperienceSection(List<ExperienceSection> experienceSection) {
        this.experienceSection = experienceSection;
    }

    public List<EducationSection> getEducationSection() {
        return educationSection;
    }

    public void setEducationSection(List<EducationSection> educationSection) {
        this.educationSection = educationSection;
    }

    public List<LanguageSection> getLanguagesSection() {
        return languagesSection;
    }

    public void setLanguagesSection(List<LanguageSection> languagesSection) {
        this.languagesSection = languagesSection;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    // Nested classes
    public static class Meta {
        @com.fasterxml.jackson.annotation.JsonProperty("job_id")
        private String jobId;
        
        @com.fasterxml.jackson.annotation.JsonProperty("job_title")
        private String jobTitle;
        
        @com.fasterxml.jackson.annotation.JsonProperty("generation_timestamp")
        private String generationTimestamp;
        
        @com.fasterxml.jackson.annotation.JsonProperty("matching_score_overall")
        private Double matchingScoreOverall;
        
        @com.fasterxml.jackson.annotation.JsonProperty("matching_details")
        private MatchingDetails matchingDetails;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getGenerationTimestamp() {
            return generationTimestamp;
        }

        public void setGenerationTimestamp(String generationTimestamp) {
            this.generationTimestamp = generationTimestamp;
        }

        public Double getMatchingScoreOverall() {
            return matchingScoreOverall;
        }

        public void setMatchingScoreOverall(Double matchingScoreOverall) {
            this.matchingScoreOverall = matchingScoreOverall;
        }

        public MatchingDetails getMatchingDetails() {
            return matchingDetails;
        }

        public void setMatchingDetails(MatchingDetails matchingDetails) {
            this.matchingDetails = matchingDetails;
        }
    }

    public static class MatchingDetails {
        @com.fasterxml.jackson.annotation.JsonProperty("skills_coverage")
        private Double skillsCoverage;
        
        @com.fasterxml.jackson.annotation.JsonProperty("domain_fit")
        private Double domainFit;
        
        @com.fasterxml.jackson.annotation.JsonProperty("experience_relevance")
        private Double experienceRelevance;

        public Double getSkillsCoverage() {
            return skillsCoverage;
        }

        public void setSkillsCoverage(Double skillsCoverage) {
            this.skillsCoverage = skillsCoverage;
        }

        public Double getDomainFit() {
            return domainFit;
        }

        public void setDomainFit(Double domainFit) {
            this.domainFit = domainFit;
        }

        public Double getExperienceRelevance() {
            return experienceRelevance;
        }

        public void setExperienceRelevance(Double experienceRelevance) {
            this.experienceRelevance = experienceRelevance;
        }
    }

    public static class Header {
        private String name;
        private String title;
        private String location;
        private String phone;
        private String email;
        private String linkedin;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getLinkedin() {
            return linkedin;
        }

        public void setLinkedin(String linkedin) {
            this.linkedin = linkedin;
        }
    }

    public static class SkillsSection {
        @com.fasterxml.jackson.annotation.JsonProperty("highlighted_skills")
        private List<String> highlightedSkills;
        
        @com.fasterxml.jackson.annotation.JsonProperty("skills_grouped")
        private Map<String, List<String>> skillsGrouped;

        public List<String> getHighlightedSkills() {
            return highlightedSkills;
        }

        public void setHighlightedSkills(List<String> highlightedSkills) {
            this.highlightedSkills = highlightedSkills;
        }

        public Map<String, List<String>> getSkillsGrouped() {
            return skillsGrouped;
        }

        public void setSkillsGrouped(Map<String, List<String>> skillsGrouped) {
            this.skillsGrouped = skillsGrouped;
        }
    }

    public static class ExperienceSection {
        private String company;
        private String country;
        private String title;
        private String start;
        private String end;
        
        @com.fasterxml.jackson.annotation.JsonProperty("relevance_score")
        private Double relevanceScore;
        
        private List<String> bullets;
        
        @com.fasterxml.jackson.annotation.JsonProperty("tech_stack")
        private List<String> techStack;

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public Double getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(Double relevanceScore) {
            this.relevanceScore = relevanceScore;
        }

        public List<String> getBullets() {
            return bullets;
        }

        public void setBullets(List<String> bullets) {
            this.bullets = bullets;
        }

        public List<String> getTechStack() {
            return techStack;
        }

        public void setTechStack(List<String> techStack) {
            this.techStack = techStack;
        }
    }

    public static class EducationSection {
        private String degree;
        private String institution;
        private String country;
        private String start;
        private String end;

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getInstitution() {
            return institution;
        }

        public void setInstitution(String institution) {
            this.institution = institution;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    public static class LanguageSection {
        private String name;
        private String level;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }

    public static class Output {
        private String markdown;
        private String html;

        @com.fasterxml.jackson.annotation.JsonProperty("pdf_base64")
        private String pdfBase64;

        @com.fasterxml.jackson.annotation.JsonProperty("docx_base64")
        private String docxBase64;

        public String getMarkdown() {
            return markdown;
        }

        public void setMarkdown(String markdown) {
            this.markdown = markdown;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public String getPdfBase64() {
            return pdfBase64;
        }

        public void setPdfBase64(String pdfBase64) {
            this.pdfBase64 = pdfBase64;
        }

        public String getDocxBase64() {
            return docxBase64;
        }

        public void setDocxBase64(String docxBase64) {
            this.docxBase64 = docxBase64;
        }
    }
}
