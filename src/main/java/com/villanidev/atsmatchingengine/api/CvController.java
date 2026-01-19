package com.villanidev.atsmatchingengine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.matching.MatchingEngine;
import com.villanidev.atsmatchingengine.service.CvFileParser;
import com.villanidev.atsmatchingengine.service.JobDescriptionParser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final MatchingEngine matchingEngine;
    private final CvFileParser cvFileParser;
    private final JobDescriptionParser jobDescriptionParser;
    private final ObjectMapper objectMapper;

    public CvController(
            MatchingEngine matchingEngine,
            CvFileParser cvFileParser,
            JobDescriptionParser jobDescriptionParser,
            ObjectMapper objectMapper) {
        this.matchingEngine = matchingEngine;
        this.cvFileParser = cvFileParser;
        this.jobDescriptionParser = jobDescriptionParser;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/generate")
    public ResponseEntity<CvGenerateResponse> generateCv(@Valid @RequestBody CvGenerateRequest request) {
        CvGenerated cvGenerated = matchingEngine.generateCv(
                request.getCvMaster(),
                request.getJob(),
                request.getOptions()
        );

        CvGenerateResponse response = new CvGenerateResponse(cvGenerated);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/generate-upload", consumes = "multipart/form-data")
    public ResponseEntity<?> generateCvFromUpload(
            @RequestParam("cv_file") MultipartFile cvFile,
            @RequestParam("job_description") String jobDescription,
            @RequestParam(value = "options", required = false) String optionsJson) {
        
        try {
            // Parse CV file
            CvMaster cvMaster = cvFileParser.parse(cvFile);
            
            // Parse job description
            Job job = jobDescriptionParser.parse(jobDescription);
            
            // Parse options if provided
            Options options = null;
            if (optionsJson != null && !optionsJson.trim().isEmpty()) {
                options = objectMapper.readValue(optionsJson, Options.class);
            }
            
            // Generate CV
            CvGenerated cvGenerated = matchingEngine.generateCv(cvMaster, job, options);
            
            CvGenerateResponse response = new CvGenerateResponse(cvGenerated);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid input: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error processing file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error generating CV: " + e.getMessage()));
        }
    }
    
    // Simple error response class
    private static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}
