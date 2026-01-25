package com.villanidev.atsmatchingengine.elt;

import java.util.List;

public class RequirementsExtractionResult {

    private final List<String> skills;
    private final List<String> tools;
    private final List<String> domains;
    private final List<String> methodologies;

    public RequirementsExtractionResult(
            List<String> skills,
            List<String> tools,
            List<String> domains,
            List<String> methodologies) {
        this.skills = skills;
        this.tools = tools;
        this.domains = domains;
        this.methodologies = methodologies;
    }

    public List<String> getSkills() {
        return skills;
    }

    public List<String> getTools() {
        return tools;
    }

    public List<String> getDomains() {
        return domains;
    }

    public List<String> getMethodologies() {
        return methodologies;
    }
}
