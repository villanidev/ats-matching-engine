package com.villanidev.atsmatchingengine.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CvMaster {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String title;
    
    private List<String> nationalities;
    
    private String phone;
    
    @NotBlank
    private String email;
    
    private String linkedin;
    
    private String location;
    
    private List<String> summary;
    
    private List<Certification> certifications;
    
    @NotNull
    private List<Skill> skills;
    
    private List<String> domains;
    
    private List<Language> languages;
    
    @NotNull
    private List<Experience> experiences;
    
    private List<Education> education;

    // Getters and Setters
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

    public List<String> getNationalities() {
        return nationalities;
    }

    public void setNationalities(List<String> nationalities) {
        this.nationalities = nationalities;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getSummary() {
        return summary;
    }

    public void setSummary(List<String> summary) {
        this.summary = summary;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Certification> certifications) {
        this.certifications = certifications;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education;
    }

    // Nested classes
    public static class Certification {
        private String name;
        private String url;
        
        @com.fasterxml.jackson.annotation.JsonProperty("valid_from")
        private String validFrom;
        
        @com.fasterxml.jackson.annotation.JsonProperty("valid_to")
        private String validTo;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getValidFrom() {
            return validFrom;
        }

        public void setValidFrom(String validFrom) {
            this.validFrom = validFrom;
        }

        public String getValidTo() {
            return validTo;
        }

        public void setValidTo(String validTo) {
            this.validTo = validTo;
        }
    }

    public static class Skill {
        @NotBlank
        private String name;
        private List<String> versions;
        private String level; // beginner | intermediate | advanced | expert

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getVersions() {
            return versions;
        }

        public void setVersions(List<String> versions) {
            this.versions = versions;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }

    public static class Language {
        @NotBlank
        private String name;
        private String level; // basic | intermediate | advanced | fluent | native

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

    public static class Experience {
        @NotBlank
        private String company;
        private String country;
        @NotBlank
        private String title;
        @NotBlank
        private String start;
        @NotBlank
        private String end; // or "present"
        private List<Project> projects;

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

        public List<Project> getProjects() {
            return projects;
        }

        public void setProjects(List<Project> projects) {
            this.projects = projects;
        }
    }

    public static class Project {
        private String name;
        private String period;
        private String situation;
        private String task;
        private List<String> actions;
        private String result;
        
        @com.fasterxml.jackson.annotation.JsonProperty("tech_stack")
        private List<String> techStack;
        
        @com.fasterxml.jackson.annotation.JsonProperty("team_size")
        private String teamSize;
        
        private List<String> domains;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getSituation() {
            return situation;
        }

        public void setSituation(String situation) {
            this.situation = situation;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public List<String> getActions() {
            return actions;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public List<String> getTechStack() {
            return techStack;
        }

        public void setTechStack(List<String> techStack) {
            this.techStack = techStack;
        }

        public String getTeamSize() {
            return teamSize;
        }

        public void setTeamSize(String teamSize) {
            this.teamSize = teamSize;
        }

        public List<String> getDomains() {
            return domains;
        }

        public void setDomains(List<String> domains) {
            this.domains = domains;
        }
    }

    public static class Education {
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
}
