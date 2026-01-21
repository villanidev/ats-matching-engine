package com.villanidev.atsmatchingengine.upload;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvUploadParser {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d[\\d\\s().-]{7,})");
    private static final Pattern SECTION_HEADER_PATTERN = Pattern.compile("^[A-Z][A-Z\\s]{2,}$");
    private static final Pattern YEARS_EXPERIENCE_PATTERN = Pattern.compile("(\\d{1,2})\\+?\\s+years", Pattern.CASE_INSENSITIVE);

    private final Tika tika = new Tika();

    public CvMaster parseCvFile(MultipartFile cvFile) {
        if (cvFile == null || cvFile.isEmpty()) {
            throw new InvalidUploadException("CV file is required.");
        }

        String text = extractText(cvFile);
        if (text.isBlank()) {
            throw new InvalidUploadException("CV file is empty or unreadable.");
        }

        return parseCvText(text);
    }

    public Job parseJobInput(MultipartFile jobFile, String jobText) {
        String text = null;
        if (jobFile != null && !jobFile.isEmpty()) {
            text = extractText(jobFile);
        } else if (jobText != null && !jobText.isBlank()) {
            text = jobText;
        }

        if (text == null || text.isBlank()) {
            throw new InvalidUploadException("Provide job_text or job_file.");
        }

        return parseJobText(text);
    }

    private String extractText(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            throw new InvalidUploadException("Failed to parse file: " + file.getOriginalFilename());
        }
    }

    private CvMaster parseCvText(String text) {
        List<String> lines = text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        CvMaster cvMaster = new CvMaster();
        cvMaster.setSkills(new ArrayList<>());
        cvMaster.setExperiences(new ArrayList<>());

        String name = extractName(lines);
        cvMaster.setName(name != null ? name : "Unknown");

        String title = extractTitle(lines);
        cvMaster.setTitle(title.isBlank() ? "Candidate" : title);

        extractContacts(lines, cvMaster);
        cvMaster.setSummary(extractSummary(lines));
        cvMaster.setSkills(extractSkills(lines));
        cvMaster.setDomains(extractDomains(lines));

        return cvMaster;
    }

    private String extractName(List<String> lines) {
        for (String line : lines) {
            if (!isContactLine(line) && !isSectionHeader(line)) {
                return line;
            }
        }
        return lines.isEmpty() ? null : lines.get(0);
    }

    private String extractTitle(List<String> lines) {
        boolean nameFound = false;
        for (String line : lines) {
            if (!nameFound && !isContactLine(line) && !isSectionHeader(line)) {
                nameFound = true;
                continue;
            }
            if (nameFound && !isContactLine(line) && !isSectionHeader(line)) {
                return line;
            }
        }
        return "";
    }

    private void extractContacts(List<String> lines, CvMaster cvMaster) {
        for (String line : lines) {
            if (cvMaster.getEmail() == null) {
                Matcher emailMatcher = EMAIL_PATTERN.matcher(line);
                if (emailMatcher.find()) {
                    cvMaster.setEmail(emailMatcher.group());
                }
            }
            if (cvMaster.getPhone() == null) {
                Matcher phoneMatcher = PHONE_PATTERN.matcher(line);
                if (phoneMatcher.find()) {
                    cvMaster.setPhone(phoneMatcher.group().trim());
                }
            }
        }

        if (cvMaster.getEmail() == null) {
            cvMaster.setEmail("unknown@example.com");
        }
    }

    private List<String> extractSummary(List<String> lines) {
        List<String> summary = new ArrayList<>();
        for (int i = 2; i < Math.min(lines.size(), 6); i++) {
            String line = lines.get(i);
            if (isSectionHeader(line)) {
                break;
            }
            if (!isContactLine(line)) {
                summary.add(line);
            }
        }
        return summary;
    }

    private List<CvMaster.Skill> extractSkills(List<String> lines) {
        List<CvMaster.Skill> skills = new ArrayList<>();
        boolean inSkillsSection = false;

        for (String line : lines) {
            String normalized = line.toLowerCase();

            if (normalized.startsWith("skills") || normalized.startsWith("technical skills") || normalized.startsWith("technologies")) {
                inSkillsSection = true;
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    addSkillsFromText(skills, inline[1]);
                }
                continue;
            }

            if (inSkillsSection) {
                if (isSectionHeader(line)) {
                    break;
                }
                addSkillsFromText(skills, line);
            }
        }

        if (skills.isEmpty()) {
            for (String line : lines) {
                if (line.contains(",") && line.split(",").length >= 3) {
                    addSkillsFromText(skills, line);
                }
            }
        }

        return deduplicateSkills(skills);
    }

    private List<String> extractDomains(List<String> lines) {
        List<String> domains = new ArrayList<>();
        boolean inDomains = false;

        for (String line : lines) {
            String normalized = line.toLowerCase();
            if (normalized.startsWith("domains") || normalized.startsWith("industries") || normalized.startsWith("industry")) {
                inDomains = true;
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    addTokens(domains, inline[1]);
                }
                continue;
            }

            if (inDomains) {
                if (isSectionHeader(line)) {
                    break;
                }
                addTokens(domains, line);
            }
        }

        return deduplicate(domains);
    }

    private void addSkillsFromText(List<CvMaster.Skill> skills, String text) {
        String[] tokens = text.split("[,;|/]");
        for (String token : tokens) {
            String cleaned = token.trim();
            if (!cleaned.isBlank()) {
                CvMaster.Skill skill = new CvMaster.Skill();
                skill.setName(cleaned);
                skills.add(skill);
            }
        }
    }

    private List<CvMaster.Skill> deduplicateSkills(List<CvMaster.Skill> skills) {
        Map<String, CvMaster.Skill> unique = new LinkedHashMap<>();
        for (CvMaster.Skill skill : skills) {
            if (skill.getName() == null) {
                continue;
            }
            String key = skill.getName().trim().toLowerCase();
            unique.putIfAbsent(key, skill);
        }
        return new ArrayList<>(unique.values());
    }

    private boolean isContactLine(String line) {
        return EMAIL_PATTERN.matcher(line).find() || PHONE_PATTERN.matcher(line).find();
    }

    private boolean isSectionHeader(String line) {
        if (line.length() > 40) {
            return false;
        }
        return SECTION_HEADER_PATTERN.matcher(line.replaceAll("[.:]", "").trim()).matches();
    }

    private Job parseJobText(String text) {
        List<String> lines = text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        Job job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setTitle(extractJobTitle(lines));
        job.setRawDescription(text);

        Job.Requirements requirements = new Job.Requirements();
        requirements.setMustHaveSkills(new ArrayList<>());
        requirements.setNiceToHaveSkills(new ArrayList<>());
        requirements.setTools(new ArrayList<>());
        requirements.setDomains(new ArrayList<>());
        job.setRequirements(requirements);

        extractJobRequirements(lines, requirements, job);

        return job;
    }

    private String extractJobTitle(List<String> lines) {
        for (String line : lines) {
            String normalized = line.toLowerCase();
            if (normalized.startsWith("title:") || normalized.startsWith("position:")) {
                return line.split(":", 2)[1].trim();
            }
        }
        return lines.isEmpty() ? "Job" : lines.get(0);
    }

    private void extractJobRequirements(List<String> lines, Job.Requirements requirements, Job job) {
        boolean inRequirements = false;
        boolean inResponsibilities = false;
        for (String line : lines) {
            String normalized = line.toLowerCase();

            Matcher yearsMatcher = YEARS_EXPERIENCE_PATTERN.matcher(line);
            if (yearsMatcher.find()) {
                requirements.setYearsOfExperience(Integer.parseInt(yearsMatcher.group(1)));
            }

            if (normalized.startsWith("requirements") || normalized.startsWith("must have") || normalized.startsWith("must-have")) {
                inRequirements = true;
                inResponsibilities = false;
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    addTokens(requirements.getMustHaveSkills(), inline[1]);
                }
                continue;
            }

            if (normalized.startsWith("nice to have") || normalized.startsWith("nice-to-have")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    addTokens(requirements.getNiceToHaveSkills(), inline[1]);
                }
                continue;
            }

            if (normalized.startsWith("tools") || normalized.startsWith("stack") || normalized.startsWith("tech stack")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    addTokens(requirements.getTools(), inline[1]);
                }
                continue;
            }

            if (normalized.startsWith("domains") || normalized.startsWith("industries") || normalized.startsWith("industry")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    addTokens(requirements.getDomains(), inline[1]);
                }
                continue;
            }

            if (normalized.startsWith("soft skills")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    job.setSoftSkills(deduplicate(addTokensToNewList(inline[1])));
                }
                continue;
            }

            if (normalized.startsWith("responsibilities") || normalized.startsWith("responsibility")) {
                inResponsibilities = true;
                inRequirements = false;
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    job.setResponsibilities(deduplicate(addTokensToNewList(inline[1])));
                }
                continue;
            }

            if (inRequirements) {
                if (isSectionHeader(line)) {
                    inRequirements = false;
                    continue;
                }
                addTokens(requirements.getMustHaveSkills(), line);
            }

            if (inResponsibilities) {
                if (isSectionHeader(line)) {
                    inResponsibilities = false;
                    continue;
                }
                if (job.getResponsibilities() == null) {
                    job.setResponsibilities(new ArrayList<>());
                }
                job.getResponsibilities().add(line);
            }
        }

        requirements.setMustHaveSkills(deduplicate(requirements.getMustHaveSkills()));
        requirements.setNiceToHaveSkills(deduplicate(requirements.getNiceToHaveSkills()));
        requirements.setTools(deduplicate(requirements.getTools()));
        requirements.setDomains(deduplicate(requirements.getDomains()));
    }

    private void addTokens(List<String> target, String text) {
        String[] tokens = text.split("[,;|/]");
        for (String token : tokens) {
            String cleaned = token.trim();
            if (!cleaned.isBlank()) {
                target.add(cleaned);
            }
        }
    }

    private List<String> addTokensToNewList(String text) {
        List<String> values = new ArrayList<>();
        addTokens(values, text);
        return values;
    }

    private List<String> deduplicate(List<String> values) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                unique.add(value.trim());
            }
        }
        return new ArrayList<>(unique);
    }
}
