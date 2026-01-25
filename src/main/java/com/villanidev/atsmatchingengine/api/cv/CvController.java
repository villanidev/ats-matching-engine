package com.villanidev.atsmatchingengine.api.cv;

import com.villanidev.atsmatchingengine.cv.CvBatchMatchingService;
import com.villanidev.atsmatchingengine.cv.CvMatchingService;
import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedStoreService;
import com.villanidev.atsmatchingengine.cv.storage.CvMasterStoreService;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.parsing.CvUploadParser;
import jakarta.validation.Valid;
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
import java.util.List;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final CvMatchingService cvMatchingService;
    private final CvMasterStoreService cvMasterStoreService;
    private final CvBatchMatchingService cvBatchMatchingService;
    private final CvGeneratedStoreService cvGeneratedStoreService;
    private final CvUploadParser cvUploadParser;

    public CvController(
            CvMatchingService cvMatchingService,
            CvMasterStoreService cvMasterStoreService,
            CvBatchMatchingService cvBatchMatchingService,
            CvGeneratedStoreService cvGeneratedStoreService,
            CvUploadParser cvUploadParser) {
        this.cvMatchingService = cvMatchingService;
        this.cvMasterStoreService = cvMasterStoreService;
        this.cvBatchMatchingService = cvBatchMatchingService;
        this.cvGeneratedStoreService = cvGeneratedStoreService;
        this.cvUploadParser = cvUploadParser;
    }

    @PostMapping("/master")
    public ResponseEntity<CvMasterSaveResponse> saveCvMaster(@Valid @RequestBody CvMaster cvMaster) {
        return ResponseEntity.ok(new CvMasterSaveResponse(cvMasterStoreService.save(cvMaster)));
    }

    @PostMapping(value = "/master-upload", consumes = "multipart/form-data")
    public ResponseEntity<CvMasterSaveResponse> saveCvMasterFromUpload(
            @RequestPart("cv_file") MultipartFile cvFile) {
        CvMaster cvMaster = cvUploadParser.parseCvFile(cvFile);
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
}