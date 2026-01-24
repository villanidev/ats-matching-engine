package com.villanidev.atsmatchingengine.elt;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RequirementsExtractor {

    private static final List<String> SKILL_DICTIONARY = List.of(
            "java", "spring", "spring boot", "kotlin", "scala",
            "python", "django", "flask", "fastapi",
            "node", "node.js", "javascript", "typescript",
            "go", "golang", "rust",
            "postgres", "postgresql", "mysql", "mongodb",
            "redis", "kafka",
            "docker", "kubernetes", "k8s",
            "aws", "azure", "gcp",
            "microservices", "rest", "graphql",
            "terraform", "ansible",
            "ci/cd", "git", "linux"
    );

    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("\\bjava\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bspring\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bspring\\s*boot\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bpython\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bnode(\\.js)?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\btypescript\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bpostgres(ql)?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bmysql\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bmongodb\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bredis\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bkafka\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdocker\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bkubernetes|\\bk8s\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\baws\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bazure\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bgcp\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bmicroservices?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\brest\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bgraphql\\b", Pattern.CASE_INSENSITIVE)
    );

    public List<String> extract(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        Set<String> matches = new LinkedHashSet<>();

        for (String item : SKILL_DICTIONARY) {
            if (normalized.contains(item)) {
                matches.add(item);
            }
        }
        for (Pattern pattern : PATTERNS) {
            if (pattern.matcher(text).find()) {
                matches.add(pattern.pattern().replace("\\b", "").replace("\\s*", " ").trim());
            }
        }
        return new ArrayList<>(matches);
    }
}
