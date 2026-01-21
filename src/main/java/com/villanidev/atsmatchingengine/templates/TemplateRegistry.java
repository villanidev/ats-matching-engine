package com.villanidev.atsmatchingengine.templates;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TemplateRegistry {

    private final Map<String, CvTemplate> templates = new HashMap<>();
    private final Map<String, String> profileToTemplate = new HashMap<>();

    public TemplateRegistry() {
        templates.put("default", new DefaultMarkdownTemplate());
        registerProfileAliases();
    }

    public CvTemplate getTemplate(String name) {
        if (name == null || name.isBlank()) {
            return templates.get("default");
        }
        return templates.getOrDefault(normalizeKey(name), templates.get("default"));
    }

    public CvTemplate getTemplateForProfile(String profile) {
        if (profile == null || profile.isBlank()) {
            return templates.get("default");
        }

        String normalizedProfile = normalizeKey(profile);
        String templateName = profileToTemplate.getOrDefault(normalizedProfile, normalizedProfile);
        return templates.getOrDefault(templateName, templates.get("default"));
    }

    private void registerProfileAliases() {
        registerAlias("default", "default");
        registerAlias("general", "default");
        registerAlias("software_engineer", "default");
        registerAlias("software_engineer_senior", "default");
        registerAlias("senior_software_engineer", "default");
        registerAlias("backend_engineer", "default");
        registerAlias("frontend_engineer", "default");
        registerAlias("fullstack_engineer", "default");
        registerAlias("data_engineer", "default");
        registerAlias("data_scientist", "default");
        registerAlias("devops_engineer", "default");
        registerAlias("cloud_engineer", "default");
        registerAlias("mobile_engineer", "default");
        registerAlias("qa_engineer", "default");
        registerAlias("product_manager", "default");
    }

    private void registerAlias(String profileKey, String templateName) {
        profileToTemplate.put(normalizeKey(profileKey), normalizeKey(templateName));
    }

    private String normalizeKey(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        return normalized;
    }
}
