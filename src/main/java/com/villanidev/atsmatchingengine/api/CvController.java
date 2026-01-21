package com.villanidev.atsmatchingengine.api;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.matching.MatchingEngine;
import com.villanidev.atsmatchingengine.upload.CvUploadParser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final MatchingEngine matchingEngine;
    private final CvUploadParser cvUploadParser;

    public CvController(MatchingEngine matchingEngine, CvUploadParser cvUploadParser) {
        this.matchingEngine = matchingEngine;
        this.cvUploadParser = cvUploadParser;
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

    @PostMapping(value = "/generate-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvGenerateResponse> generateCvFromUpload(
            @RequestPart("cv_file") MultipartFile cvFile,
            @RequestPart(value = "job_file", required = false) MultipartFile jobFile,
            @RequestPart(value = "job_text", required = false) String jobText
    ) {
        CvMaster cvMaster = cvUploadParser.parseCvFile(cvFile);
        Job job = cvUploadParser.parseJobInput(jobFile, jobText);
        Options options = new Options();

        CvGenerated cvGenerated = matchingEngine.generateCv(cvMaster, job, options);
        CvGenerateResponse response = new CvGenerateResponse(cvGenerated);
        return ResponseEntity.ok(response);
    }
}
