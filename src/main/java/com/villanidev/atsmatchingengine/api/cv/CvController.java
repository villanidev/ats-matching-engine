package com.villanidev.atsmatchingengine.api.cv;

import com.villanidev.atsmatchingengine.cv.CvBatchMatchingService;
import com.villanidev.atsmatchingengine.cv.CvGenerator;
import com.villanidev.atsmatchingengine.cv.CvMatchingService;
import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedStoreService;
import com.villanidev.atsmatchingengine.cv.storage.CvMasterStoreService;
import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.parsing.CvUploadParser;
import com.villanidev.atsmatchingengine.parsing.InvalidUploadException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final CvGenerator cvGenerator;
    private final CvUploadParser cvUploadParser;
    private final CvMatchingService cvMatchingService;
    private final CvMasterStoreService cvMasterStoreService;
    private final CvBatchMatchingService cvBatchMatchingService;
    private final CvGeneratedStoreService cvGeneratedStoreService;

    public CvController(
            CvGenerator cvGenerator,
            CvUploadParser cvUploadParser,
            CvMatchingService cvMatchingService,
            CvMasterStoreService cvMasterStoreService,
            CvBatchMatchingService cvBatchMatchingService,
            CvGeneratedStoreService cvGeneratedStoreService) {
        this.cvGenerator = cvGenerator;
        this.cvUploadParser = cvUploadParser;
        this.cvMatchingService = cvMatchingService;
        this.cvMasterStoreService = cvMasterStoreService;
        this.cvBatchMatchingService = cvBatchMatchingService;
        this.cvGeneratedStoreService = cvGeneratedStoreService;
    }

    @PostMapping("/master")
    public ResponseEntity<CvMasterSaveResponse> saveCvMaster(@Valid @RequestBody CvMaster cvMaster) {
        return ResponseEntity.ok(new CvMasterSaveResponse(cvMasterStoreService.save(cvMaster)));
    }

    @GetMapping("/master")
    public ResponseEntity<List<CvMasterSummary>> listCvMasters(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<CvMasterSummary> result = cvMasterStoreService.list(limit).stream()
                .map(CvMasterSummary::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/master/{id}")
    public ResponseEntity<CvMaster> getCvMaster(@PathVariable("id") Long id) {
        return cvMasterStoreService.loadCvMaster(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/master/{id}")
    public ResponseEntity<CvMasterSaveResponse> updateCvMaster(
            @PathVariable("id") Long id,
            @Valid @RequestBody CvMaster cvMaster) {
        return cvMasterStoreService.update(id, cvMaster)
                .map(entity -> ResponseEntity.ok(new CvMasterSaveResponse(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/master/{id}")
    public ResponseEntity<Void> deleteCvMaster(@PathVariable("id") Long id) {
        boolean deleted = cvMasterStoreService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/generated")
    public ResponseEntity<List<CvGeneratedSummary>> listGenerated(
            @RequestParam("cv_master_id") Long cvMasterId,
            @RequestParam(value = "job_posting_id", required = false) Long jobPostingId) {
        List<CvGeneratedSummary> response = (jobPostingId == null
                ? cvGeneratedStoreService.listByCvMaster(cvMasterId)
                : cvGeneratedStoreService.listByCvMasterAndJob(cvMasterId, jobPostingId))
                .stream()
                .map(CvGeneratedSummary::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<CvGenerateResponse> generateCv(@Valid @RequestBody CvGenerateRequest request) {
        CvGenerated cvGenerated = cvGenerator.generate(
                request.getCvMaster(),
                request.getJob(),
                request.getOptions()
        );

        CvGenerateResponse response = new CvGenerateResponse(cvGenerated);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-from-db")
    public ResponseEntity<CvGenerateFromDbResponse> generateCvFromDb(
            @Valid @RequestBody CvGenerateFromDbRequest request) {
        CvGenerateFromDbResponse response = new CvGenerateFromDbResponse(
                cvMatchingService.generateForJob(
                        request.getCvMasterId(),
                        request.getJobPostingId(),
                        request.getOptions() != null ? request.getOptions() : new Options())
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/match/batch")
    public ResponseEntity<CvBatchMatchResponse> matchBatch(@Valid @RequestBody CvBatchMatchRequest request) {
        Options resolved = request.getOptions() != null ? request.getOptions() : new Options();
        int limit = request.getLimit() != null ? request.getLimit() : 200;
        CvBatchMatchingService.BatchResult result = cvBatchMatchingService
                .runBatch(request.getCvMasterId(), resolved, limit);
        return ResponseEntity.ok(new CvBatchMatchResponse(result));
    }

    @PostMapping(value = "/generate-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvGenerateResponse> generateCvFromUpload(
            @RequestPart("cv_file") MultipartFile cvFile,
            @RequestPart(value = "job_file", required = false) MultipartFile jobFile,
            @RequestPart(value = "job_text", required = false) String jobText,
            @RequestPart(value = "options", required = false) Options options
    ) {
        CvMaster cvMaster = cvUploadParser.parseCvFile(cvFile);
        Job job = cvUploadParser.parseJobInput(jobFile, jobText);
        Options resolvedOptions = options != null ? options : new Options();

        CvGenerated cvGenerated = cvGenerator.generate(cvMaster, job, resolvedOptions);
        CvGenerateResponse response = new CvGenerateResponse(cvGenerated);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/generate-upload/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateCvFromUploadPdf(
            @RequestPart("cv_file") MultipartFile cvFile,
            @RequestPart(value = "job_file", required = false) MultipartFile jobFile,
            @RequestPart(value = "job_text", required = false) String jobText,
            @RequestPart(value = "options", required = false) Options options
    ) {
        CvGenerated cvGenerated = generateCvForBinary(cvFile, jobFile, jobText, List.of("pdf"), options);
        String pdfBase64 = cvGenerated.getOutput().getPdfBase64();
        if (pdfBase64 == null) {
            throw new InvalidUploadException("PDF generation failed.");
        }
        byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
        String filename = buildFilename(cvGenerated, "pdf");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping(value = "/generate-upload/docx", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public ResponseEntity<byte[]> generateCvFromUploadDocx(
            @RequestPart("cv_file") MultipartFile cvFile,
            @RequestPart(value = "job_file", required = false) MultipartFile jobFile,
            @RequestPart(value = "job_text", required = false) String jobText,
            @RequestPart(value = "options", required = false) Options options
    ) {
        CvGenerated cvGenerated = generateCvForBinary(cvFile, jobFile, jobText, List.of("docx"), options);
        String docxBase64 = cvGenerated.getOutput().getDocxBase64();
        if (docxBase64 == null) {
            throw new InvalidUploadException("DOCX generation failed.");
        }
        byte[] docxBytes = Base64.getDecoder().decode(docxBase64);
        String filename = buildFilename(cvGenerated, "docx");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }

    private CvGenerated generateCvForBinary(MultipartFile cvFile,
                                            MultipartFile jobFile,
                                            String jobText,
                                            List<String> formats,
                                            Options options) {
        CvMaster cvMaster = cvUploadParser.parseCvFile(cvFile);
        Job job = cvUploadParser.parseJobInput(jobFile, jobText);
        Options resolvedOptions = options != null ? options : new Options();
        resolvedOptions.setOutputFormats(formats);

        return cvGenerator.generate(cvMaster, job, resolvedOptions);
    }

    private String buildFilename(CvGenerated cvGenerated, String extension) {
        String candidateName = sanitizeFilenamePart(cvGenerated.getHeader().getName());
        String jobTitle = sanitizeFilenamePart(cvGenerated.getMeta().getJobTitle());
        return String.format("cv_%s_for_%s.%s", candidateName, jobTitle, extension);
    }

    private String sanitizeFilenamePart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String sanitized = value.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return sanitized.isBlank() ? "unknown" : sanitized;
    }
}