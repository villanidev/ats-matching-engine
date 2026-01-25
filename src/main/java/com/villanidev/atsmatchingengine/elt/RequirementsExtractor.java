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

        private static final List<String> TOOL_DICTIONARY = List.of(
            "docker", "kubernetes", "k8s", "terraform", "ansible",
            "git", "linux", "jenkins", "github actions", "gitlab ci",
            "datadog", "new relic", "grafana", "prometheus"
        );

        private static final List<String> DOMAIN_DICTIONARY = List.of(
            "fintech", "healthtech", "e-commerce", "ecommerce", "saas",
            "marketplace", "logistics", "edtech", "adtech"
        );

        private static final List<String> METHODOLOGY_DICTIONARY = List.of(
            "agile", "scrum", "kanban", "xp", "tdd", "bdd"
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

    public RequirementsExtractionResult extractAll(String text) {
        if (text == null || text.isBlank()) {
            return new RequirementsExtractionResult(List.of(), List.of(), List.of(), List.of());
        }
        String normalized = text.toLowerCase(Locale.ROOT);

        Set<String> skills = new LinkedHashSet<>();
        Set<String> tools = new LinkedHashSet<>();
        Set<String> domains = new LinkedHashSet<>();
        Set<String> methodologies = new LinkedHashSet<>();

        matchDictionary(normalized, SKILL_DICTIONARY, skills);
        matchDictionary(normalized, TOOL_DICTIONARY, tools);
        matchDictionary(normalized, DOMAIN_DICTIONARY, domains);
        matchDictionary(normalized, METHODOLOGY_DICTIONARY, methodologies);

        for (Pattern pattern : PATTERNS) {
            if (pattern.matcher(text).find()) {
                skills.add(pattern.pattern().replace("\\b", "").replace("\\s*", " ").trim());
            }
        }

        return new RequirementsExtractionResult(
                new ArrayList<>(skills),
                new ArrayList<>(tools),
                new ArrayList<>(domains),
                new ArrayList<>(methodologies)
        );
    }

    public List<String> extract(String text) {
        return extractAll(text).getSkills();
    }

    private void matchDictionary(String normalized, List<String> dictionary, Set<String> target) {
        for (String item : dictionary) {
            if (normalized.contains(item)) {
                target.add(item);
            }
        }
    }
}
