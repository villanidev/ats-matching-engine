package com.villanidev.atsmatchingengine.upload;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvUploadParser {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d[\\d\\s().-]{7,})");
    private static final Pattern SECTION_HEADER_PATTERN = Pattern.compile("^[A-Z][A-Z\\s]{2,}$");
    private static final Pattern YEARS_EXPERIENCE_PATTERN = Pattern.compile("(\\d{1,2})\\+?\\s+years", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPERIENCE_LINE_PATTERN = Pattern.compile("^(.+?),\\s*([^\\-]+?)\\s*[-–]\\s*(.+?)\\s*[-–]\\s*([A-Z]{3}\\s\\d{4})\\s*to\\s*(current|[A-Z]{3}\\s\\d{4})$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EDUCATION_LINE_PATTERN = Pattern.compile("^(.+?)\\s*[-–]\\s*(.+?)\\s*\\((\\d{4})\\s*[-–]\\s*(\\d{4}|present)\\)$", Pattern.CASE_INSENSITIVE);

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

    public String extractTextFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidUploadException("File is required.");
        }
        String text = extractText(file);
        if (text.isBlank()) {
            throw new InvalidUploadException("File is empty or unreadable.");
        }
        return text;
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
        cvMaster.setExperiences(extractExperiences(lines));
        cvMaster.setEducation(extractEducation(lines));
        cvMaster.setLanguages(extractLanguages(lines));

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
            if (!isContactLine(line) && !isUrlLine(line) && !isSectionHeader(line) && !isSummaryHeader(line)) {
                summary.add(normalizeLine(line));
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
                    domains.addAll(addTokensNormalized(inline[1]));
                }
                continue;
            }

            if (inDomains) {
                if (isSectionHeader(line)) {
                    break;
                }
                domains.addAll(addTokensNormalized(line));
            }
        }

        return deduplicate(domains);
    }

    private void addSkillsFromText(List<CvMaster.Skill> skills, String text) {
        addTokensNormalized(text).forEach(value -> {
            CvMaster.Skill skill = new CvMaster.Skill();
            skill.setName(value);
            skills.add(skill);
        });
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

    private boolean isUrlLine(String line) {
        String normalized = line.toLowerCase();
        return normalized.startsWith("http") || normalized.startsWith("www.") || normalized.startsWith("mailto:");
    }

    private boolean isSectionHeader(String line) {
        if (line.length() > 40) {
            return false;
        }
        return SECTION_HEADER_PATTERN.matcher(line.replaceAll("[.:]", "").trim()).matches();
    }

    private boolean isSummaryHeader(String line) {
        String normalized = line.trim().toLowerCase();
        return normalized.equals("professional profile") || normalized.equals("profile") || normalized.equals("summary");
    }

    private String normalizeLine(String line) {
        String normalized = Normalizer.normalize(line, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[\\p{C}]", " ");
        normalized = normalized.replaceAll("[^\\p{L}\\p{N}+#./&\\-\\s()]", " ");
        normalized = normalized.replaceAll("\u2022|\u25AA|\u25CF|\u25A0", " ");
        normalized = normalized.replaceAll("\\s{2,}", " ").trim();
        return normalized;
    }

    private List<String> addTokensNormalized(String text) {
        List<String> values = new ArrayList<>();
        String[] tokens = text.split("[,;|/]");
        for (String token : tokens) {
            String cleaned = normalizeLine(token);
            cleaned = stripTrailingConjunction(cleaned);
            cleaned = stripDanglingParen(cleaned);
            cleaned = cleaned.replaceAll("[\\s.]+$", "").trim();
            if (isValidToken(cleaned)) {
                values.add(cleaned);
            }
        }
        return values;
    }

    private String stripTrailingConjunction(String value) {
        String normalized = value.trim();
        if (normalized.toLowerCase().endsWith(" and")) {
            return normalized.substring(0, normalized.length() - 4).trim();
        }
        if (normalized.toLowerCase().endsWith(" or")) {
            return normalized.substring(0, normalized.length() - 3).trim();
        }
        return normalized;
    }

    private String stripDanglingParen(String value) {
        int openIndex = value.indexOf('(');
        int closeIndex = value.indexOf(')');
        if (openIndex >= 0 && closeIndex < 0) {
            return value.substring(0, openIndex).trim();
        }
        return value;
    }

    private boolean isValidToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        if (token.length() > 40) {
            return false;
        }
        if (token.length() < 3) {
            Set<String> allowedShort = Set.of("AI", "Go", "C#", "C++", "C");
            return allowedShort.contains(token);
        }
        return token.chars().anyMatch(Character::isLetter);
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

    private List<CvMaster.Experience> extractExperiences(List<String> lines) {
        List<CvMaster.Experience> experiences = new ArrayList<>();
        CvMaster.Experience current = null;

        for (String line : lines) {
            Matcher matcher = EXPERIENCE_LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                current = new CvMaster.Experience();
                current.setCompany(matcher.group(1).trim());
                current.setCountry(matcher.group(2).trim());
                current.setTitle(matcher.group(3).trim());
                current.setStart(parseMonthYear(matcher.group(4)));
                current.setEnd(parseMonthYear(matcher.group(5)));
                current.setProjects(new ArrayList<>());
                experiences.add(current);
                continue;
            }

            if (current != null) {
                if (line.startsWith("Tech stack:")) {
                    List<String> tech = addTokensNormalized(line.substring("Tech stack:".length()));
                    CvMaster.Project project = new CvMaster.Project();
                    project.setTechStack(tech);
                    current.getProjects().add(project);
                } else if (line.startsWith("A:") || line.startsWith("R:") || line.startsWith("S:") || line.startsWith("T:")) {
                    CvMaster.Project project = ensureProject(current);
                    if (project.getActions() == null) {
                        project.setActions(new ArrayList<>());
                    }
                    project.getActions().add(line);
                }
            }
        }

        return experiences;
    }

    private CvMaster.Project ensureProject(CvMaster.Experience experience) {
        if (experience.getProjects() == null) {
            experience.setProjects(new ArrayList<>());
        }
        if (experience.getProjects().isEmpty()) {
            experience.getProjects().add(new CvMaster.Project());
        }
        return experience.getProjects().get(0);
    }

    private String parseMonthYear(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if ("current".equalsIgnoreCase(normalized) || "present".equalsIgnoreCase(normalized)) {
            return "present";
        }
        Map<String, String> months = Map.ofEntries(
                Map.entry("JAN", "01"), Map.entry("FEB", "02"), Map.entry("MAR", "03"), Map.entry("APR", "04"),
                Map.entry("MAY", "05"), Map.entry("JUN", "06"), Map.entry("JUL", "07"), Map.entry("AUG", "08"),
                Map.entry("SEP", "09"), Map.entry("OCT", "10"), Map.entry("NOV", "11"), Map.entry("DEC", "12")
        );
        String[] parts = normalized.split("\\s+");
        if (parts.length == 2) {
            String month = months.getOrDefault(parts[0].toUpperCase(), "01");
            return parts[1] + "-" + month;
        }
        return normalized;
    }

    private List<CvMaster.Education> extractEducation(List<String> lines) {
        List<CvMaster.Education> education = new ArrayList<>();
        boolean inEducation = false;
        for (String line : lines) {
            String normalized = line.toLowerCase();
            if (normalized.startsWith("education")) {
                inEducation = true;
                continue;
            }
            if (inEducation && isSectionHeader(line)) {
                break;
            }
            if (!inEducation) {
                continue;
            }
            Matcher matcher = EDUCATION_LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
                CvMaster.Education edu = new CvMaster.Education();
                edu.setDegree(matcher.group(1).trim());
                edu.setInstitution(matcher.group(2).trim());
                edu.setStart(matcher.group(3));
                edu.setEnd(matcher.group(4));
                education.add(edu);
            }
        }
        return education;
    }

    private List<CvMaster.Language> extractLanguages(List<String> lines) {
        List<CvMaster.Language> languages = new ArrayList<>();
        boolean inLanguages = false;
        for (String line : lines) {
            String normalized = line.toLowerCase();
            if (normalized.startsWith("languages")) {
                inLanguages = true;
                continue;
            }
            if (inLanguages && isSectionHeader(line)) {
                break;
            }
            if (!inLanguages) {
                continue;
            }
            String[] parts = line.split("[-–:]", 2);
            if (parts.length == 2) {
                CvMaster.Language language = new CvMaster.Language();
                language.setName(normalizeLine(parts[0]));
                language.setLevel(normalizeLine(parts[1]));
                languages.add(language);
            }
        }
        return languages;
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

            if (normalized.startsWith("requirements") || normalized.startsWith("must have") || normalized.startsWith("must-have")
                    || normalized.startsWith("knowledge and skills") || normalized.equals("knowledge and skills")
                    || normalized.startsWith("skills") || normalized.equals("skills")) {
                inRequirements = true;
                inResponsibilities = false;
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    requirements.getMustHaveSkills().addAll(addSkillsFromSentence(inline[1]));
                }
                continue;
            }

            if (normalized.startsWith("nice to have") || normalized.startsWith("nice-to-have")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    requirements.getNiceToHaveSkills().addAll(addSkillsFromSentence(inline[1]));
                }
                continue;
            }

            if (normalized.startsWith("tools") || normalized.startsWith("stack") || normalized.startsWith("tech stack")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    requirements.getTools().addAll(addTokensNormalized(inline[1]));
                }
                continue;
            }

            if (normalized.startsWith("domains") || normalized.startsWith("industries") || normalized.startsWith("industry")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    requirements.getDomains().addAll(addTokensNormalized(inline[1]));
                }
                continue;
            }

            if (normalized.startsWith("soft skills")) {
                String[] inline = line.split(":", 2);
                if (inline.length == 2) {
                    job.setSoftSkills(deduplicate(addSkillsFromSentence(inline[1])));
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
                requirements.getMustHaveSkills().addAll(addSkillsFromSentence(line));
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

    private List<String> addSkillsFromSentence(String sentence) {
        String cleaned = normalizeLine(sentence);
        String lower = cleaned.toLowerCase();
        String[] prefixes = {
            "strong experience in", "deep knowledge of", "strong understanding of",
            "experience in", "experience with", "knowledge of",
            "familiarity with", "hands-on experience with", "hands on experience with",
            "work with", "working with", "use of", "using", "exposure to"
        };

        for (String prefix : prefixes) {
            int index = lower.indexOf(prefix);
            if (index >= 0) {
                cleaned = cleaned.substring(index + prefix.length()).trim();
                break;
            }
        }

        cleaned = cleaned.replaceAll("\\(.*?\\)", "");
        cleaned = cleaned.replaceAll("\\s+and\\s+", ", ");
        cleaned = cleaned.replaceAll("\\s+or\\s+", ", ");
        cleaned = cleaned.replaceAll("\\s+frameworks?\\b", "");
        cleaned = cleaned.replaceAll("\\s+principles\\b", "");
        cleaned = cleaned.replaceAll("\\s+practices\\b", "");
        cleaned = cleaned.replaceAll("\\s+architecture\\b", "");

        return addTokensNormalized(cleaned);
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
