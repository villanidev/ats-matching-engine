package com.villanidev.atsmatchingengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CvFileParser {
    
    private static final String DEFAULT_TITLE = "N/A";
    private static final String DEFAULT_EMAIL = "candidate@example.com";
    
    private final ObjectMapper objectMapper;
    
    public CvFileParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Parses an uploaded CV file into a CvMaster object.
     * 
     * @param file the uploaded file
     * @return CvMaster instance
     * @throws IOException if file reading or parsing fails
     */
    public CvMaster parse(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CV file cannot be empty");
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        // Check if it's a JSON file
        if (isJsonFile(contentType, filename)) {
            return parseJsonCv(file);
        } else {
            return parseTextCv(file);
        }
    }
    
    private boolean isJsonFile(String contentType, String filename) {
        if (contentType != null && contentType.equals("application/json")) {
            return true;
        }
        if (filename != null && filename.toLowerCase().endsWith(".json")) {
            return true;
        }
        return false;
    }
    
    private CvMaster parseJsonCv(MultipartFile file) throws IOException {
        return objectMapper.readValue(file.getInputStream(), CvMaster.class);
    }
    
    private CvMaster parseTextCv(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        
        CvMaster cvMaster = new CvMaster();
        
        // Extract name from first non-blank line
        String name = extractFirstNonBlankLine(content);
        cvMaster.setName(name != null ? name : "Unknown Candidate");
        
        // Set placeholder values for required fields
        cvMaster.setTitle(DEFAULT_TITLE);
        cvMaster.setEmail(DEFAULT_EMAIL);
        
        // Summary contains the full text
        List<String> summary = new ArrayList<>();
        summary.add(content);
        cvMaster.setSummary(summary);
        
        // Initialize required lists as empty
        cvMaster.setSkills(new ArrayList<>());
        cvMaster.setExperiences(new ArrayList<>());
        
        return cvMaster;
    }
    
    private String extractFirstNonBlankLine(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return null;
    }
}
