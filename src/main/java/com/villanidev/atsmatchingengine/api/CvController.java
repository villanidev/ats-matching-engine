package com.villanidev.atsmatchingengine.api;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.matching.MatchingEngine;
import com.villanidev.atsmatchingengine.upload.CvUploadParser;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

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

    @PostMapping(value = "/generate-upload/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateCvFromUploadPdf(
            @RequestPart("cv_file") MultipartFile cvFile,
            @RequestPart(value = "job_file", required = false) MultipartFile jobFile,
            @RequestPart(value = "job_text", required = false) String jobText
    ) {
        CvGenerated cvGenerated = generateCvForBinary(cvFile, jobFile, jobText, List.of("pdf"));
        String pdfBase64 = cvGenerated.getOutput().getPdfBase64();
        if (pdfBase64 == null) {
            throw new com.villanidev.atsmatchingengine.upload.InvalidUploadException("PDF generation failed.");
        }
        byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-cv.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping(value = "/generate-upload/docx", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public ResponseEntity<byte[]> generateCvFromUploadDocx(
            @RequestPart("cv_file") MultipartFile cvFile,
            @RequestPart(value = "job_file", required = false) MultipartFile jobFile,
            @RequestPart(value = "job_text", required = false) String jobText
    ) {
        CvGenerated cvGenerated = generateCvForBinary(cvFile, jobFile, jobText, List.of("docx"));
        String docxBase64 = cvGenerated.getOutput().getDocxBase64();
        if (docxBase64 == null) {
            throw new com.villanidev.atsmatchingengine.upload.InvalidUploadException("DOCX generation failed.");
        }
        byte[] docxBytes = Base64.getDecoder().decode(docxBase64);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-cv.docx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }

    private CvGenerated generateCvForBinary(MultipartFile cvFile,
                                            MultipartFile jobFile,
                                            String jobText,
                                            List<String> formats) {
        CvMaster cvMaster = cvUploadParser.parseCvFile(cvFile);
        Job job = cvUploadParser.parseJobInput(jobFile, jobText);
        Options options = new Options();
        options.setOutputFormats(formats);

        return matchingEngine.generateCv(cvMaster, job, options);
    }
}
