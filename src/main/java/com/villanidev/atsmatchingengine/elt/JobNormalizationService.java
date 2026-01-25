package com.villanidev.atsmatchingengine.elt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobNormalizationService {

    private static final Logger logger = LoggerFactory.getLogger(JobNormalizationService.class);

    private final ObjectMapper objectMapper;
    private final RequirementsExtractor requirementsExtractor;

    public JobNormalizationService(ObjectMapper objectMapper, RequirementsExtractor requirementsExtractor) {
        this.objectMapper = objectMapper;
        this.requirementsExtractor = requirementsExtractor;
    }

    public Optional<JobPostingNormalized> normalize(JobPostingRaw raw) {
        if (raw == null || raw.getRawContent() == null || raw.getRawContent().isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(raw.getRawContent());
            JobPostingNormalized normalized = new JobPostingNormalized();
            normalized.setRawId(raw.getId());
            normalized.setSource(raw.getSource());
            normalized.setExternalId(raw.getExternalId());
            normalized.setTitle(extractTitle(raw.getSource(), root));
            String description = cleanText(extractDescription(raw.getSource(), root));
            String requirements = cleanText(extractRequirements(raw.getSource(), root));
            normalized.setDescription(description);
            normalized.setRequirements(enrichRequirements(description, requirements));
            normalized.setCompany(extractCompany(raw.getSource(), root));
            normalized.setLocation(extractLocation(raw.getSource(), root));
            normalized.setNormalizedAt(LocalDateTime.now());
            logger.debug("Normalized RAW job id={} source={} externalId={}", raw.getId(), raw.getSource(), raw.getExternalId());
            return Optional.of(normalized);
        } catch (Exception ex) {
            logger.info("Failed to normalize RAW job id={} source={} message={}", raw.getId(), raw.getSource(), ex.getMessage());
            return Optional.empty();
        }
    }

    private String extractTitle(String source, JsonNode root) {
        if ("greenhouse".equalsIgnoreCase(source)) {
            return firstText(root, "title");
        }
        if ("breezy".equalsIgnoreCase(source)) {
            return firstText(root, "name", "title");
        }
        if ("ashby".equalsIgnoreCase(source)) {
            return firstText(root, "title", "name");
        }
        return firstText(root, "title", "name");
    }

    private String extractDescription(String source, JsonNode root) {
        if ("greenhouse".equalsIgnoreCase(source)) {
            return firstText(root, "content", "description");
        }
        if ("breezy".equalsIgnoreCase(source)) {
            return firstText(root, "description", "descriptionHtml", "descriptionText");
        }
        if ("ashby".equalsIgnoreCase(source)) {
            return firstText(root, "descriptionHtml", "description", "descriptionPlain");
        }
        return firstText(root, "description", "content");
    }

    private String extractRequirements(String source, JsonNode root) {
        if ("breezy".equalsIgnoreCase(source)) {
            return firstText(root, "requirements");
        }
        if ("ashby".equalsIgnoreCase(source)) {
            return firstText(root, "requirements", "descriptionRequirements", "requirementsSummary");
        }
        return firstText(root, "requirements", "requirementsSummary");
    }

    private String extractCompany(String source, JsonNode root) {
        if ("greenhouse".equalsIgnoreCase(source)) {
            return firstText(root, "company", "companyName");
        }
        if ("ashby".equalsIgnoreCase(source)) {
            return firstText(root, "companyName", "department", "team");
        }
        return firstText(root, "company", "companyName", "team");
    }

    private String extractLocation(String source, JsonNode root) {
        if ("greenhouse".equalsIgnoreCase(source)) {
            return firstText(root, "location.name", "location", "locationName");
        }
        if ("ashby".equalsIgnoreCase(source)) {
            return firstText(root, "location", "locationName", "location.name");
        }
        if ("breezy".equalsIgnoreCase(source)) {
            return firstText(root, "location", "locationName", "location.name");
        }
        return firstText(root, "location", "location.name");
    }

    private String firstText(JsonNode root, String... paths) {
        for (String path : paths) {
            String value = textAtPath(root, path);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String textAtPath(JsonNode root, String path) {
        if (root == null || path == null || path.isBlank()) {
            return null;
        }
        JsonNode current = root;
        for (String part : path.split("\\.")) {
            current = current.get(part);
            if (current == null) {
                return null;
            }
        }
        if (current.isTextual()) {
            return current.asText();
        }
        if (current.isObject() || current.isArray()) {
            return current.toString();
        }
        return current.asText(null);
    }

    private String cleanText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String cleaned = value;
        cleaned = cleaned.replaceAll("(?i)<br\\s*/?>", "\n");
        cleaned = cleaned.replaceAll("(?s)<[^>]*>", " ");
        cleaned = cleaned.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private String enrichRequirements(String description, String requirements) {
        String base = requirements != null && !requirements.isBlank() ? requirements : "";
        String text = (description != null ? description : "") + "\n" + base;
        RequirementsExtractionResult extracted = requirementsExtractor.extractAll(text);
        if (extracted.getSkills().isEmpty() && extracted.getTools().isEmpty() && extracted.getDomains().isEmpty()) {
            return base.isBlank() ? null : base;
        }
        StringBuilder builder = new StringBuilder();
        if (!base.isBlank()) {
            builder.append(base).append("\n");
        }
        extracted.getSkills().forEach(item -> builder.append(item).append("\n"));
        extracted.getTools().forEach(item -> builder.append(item).append("\n"));
        extracted.getDomains().forEach(item -> builder.append(item).append("\n"));
        return builder.toString().trim();
    }
}
