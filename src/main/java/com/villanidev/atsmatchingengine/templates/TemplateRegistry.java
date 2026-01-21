package com.villanidev.atsmatchingengine.templates;

import java.util.HashMap;
import java.util.Map;

public class TemplateRegistry {

    private final Map<String, CvTemplate> templates = new HashMap<>();

    public TemplateRegistry() {
        templates.put("default", new DefaultMarkdownTemplate());
    }

    public CvTemplate getTemplate(String name) {
        if (name == null || name.isBlank()) {
            return templates.get("default");
        }
        return templates.getOrDefault(name.toLowerCase(), templates.get("default"));
    }
}
