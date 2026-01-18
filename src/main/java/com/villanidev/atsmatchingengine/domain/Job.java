package com.villanidev.atsmatchingengine.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class Job {
    
    @NotBlank
    private String id;
    
    @NotBlank
    private String title;
    
    private String location;
    
    private String seniority; // junior | mid | senior | lead | principal
    
    @com.fasterxml.jackson.annotation.JsonProperty("raw_description")
    private String rawDescription;
    
    @NotNull
    private Requirements requirements;
    
    private List<String> responsibilities;
    
    @com.fasterxml.jackson.annotation.JsonProperty("soft_skills")
    private List<String> softSkills;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSeniority() {
        return seniority;
    }

    public void setSeniority(String seniority) {
        this.seniority = seniority;
    }

    public String getRawDescription() {
        return rawDescription;
    }

    public void setRawDescription(String rawDescription) {
        this.rawDescription = rawDescription;
    }

    public Requirements getRequirements() {
        return requirements;
    }

    public void setRequirements(Requirements requirements) {
        this.requirements = requirements;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public List<String> getSoftSkills() {
        return softSkills;
    }

    public void setSoftSkills(List<String> softSkills) {
        this.softSkills = softSkills;
    }

    // Nested classes
    public static class Requirements {
        @com.fasterxml.jackson.annotation.JsonProperty("years_of_experience")
        private Integer yearsOfExperience;
        
        @com.fasterxml.jackson.annotation.JsonProperty("must_have_skills")
        private List<String> mustHaveSkills;
        
        @com.fasterxml.jackson.annotation.JsonProperty("nice_to_have_skills")
        private List<String> niceToHaveSkills;
        
        private List<String> tools;
        private List<String> methodologies;
        private List<String> domains;
        private List<LanguageRequirement> languages;

        public Integer getYearsOfExperience() {
            return yearsOfExperience;
        }

        public void setYearsOfExperience(Integer yearsOfExperience) {
            this.yearsOfExperience = yearsOfExperience;
        }

        public List<String> getMustHaveSkills() {
            return mustHaveSkills;
        }

        public void setMustHaveSkills(List<String> mustHaveSkills) {
            this.mustHaveSkills = mustHaveSkills;
        }

        public List<String> getNiceToHaveSkills() {
            return niceToHaveSkills;
        }

        public void setNiceToHaveSkills(List<String> niceToHaveSkills) {
            this.niceToHaveSkills = niceToHaveSkills;
        }

        public List<String> getTools() {
            return tools;
        }

        public void setTools(List<String> tools) {
            this.tools = tools;
        }

        public List<String> getMethodologies() {
            return methodologies;
        }

        public void setMethodologies(List<String> methodologies) {
            this.methodologies = methodologies;
        }

        public List<String> getDomains() {
            return domains;
        }

        public void setDomains(List<String> domains) {
            this.domains = domains;
        }

        public List<LanguageRequirement> getLanguages() {
            return languages;
        }

        public void setLanguages(List<LanguageRequirement> languages) {
            this.languages = languages;
        }
    }

    public static class LanguageRequirement {
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
}
