package com.villanidev.atsmatchingengine.upload;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

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
        } catch (IOException | TikaException | SAXException e) {
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

        String name = lines.isEmpty() ? null : lines.get(0);
        cvMaster.setName(name != null ? name : "Unknown");

        String title = lines.size() > 1 ? lines.get(1) : "";
        if (isContactLine(title)) {
            title = "";
        }
        cvMaster.setTitle(title.isBlank() ? "" : title);

        extractContacts(lines, cvMaster);
        cvMaster.setSummary(extractSummary(lines));
        cvMaster.setSkills(extractSkills(lines));

        return cvMaster;
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

            if (normalized.startsWith("skills") || normalized.startsWith("technical skills")) {
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

        return skills;
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
}
