package com.villanidev.atsmatchingengine.service;

import com.villanidev.atsmatchingengine.domain.Job;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobDescriptionParser {
    
    /**
     * Parses a plain text job description into a Job object.
     * This is a P0 implementation that creates a minimal Job structure.
     * 
     * @param jobDescription the plain text job description
     * @return Job instance with basic requirements
     */
    public Job parse(String jobDescription) {
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }
        
        Job job = new Job();
        
        // Generate a unique job ID
        job.setId("job-" + UUID.randomUUID().toString());
        
        // Extract title from first line or use default
        String title = extractTitle(jobDescription);
        job.setTitle(title);
        
        // Store the raw description
        job.setRawDescription(jobDescription);
        
        // Create minimal requirements object
        Job.Requirements requirements = new Job.Requirements();
        job.setRequirements(requirements);
        
        return job;
    }
    
    private String extractTitle(String jobDescription) {
        String[] lines = jobDescription.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                // Use first non-blank line as title, limit to 100 characters
                return trimmed.length() > 100 ? trimmed.substring(0, 100) : trimmed;
            }
        }
        return "Job Position";
    }
}
