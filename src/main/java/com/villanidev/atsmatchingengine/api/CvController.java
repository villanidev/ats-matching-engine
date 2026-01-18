package com.villanidev.atsmatchingengine.api;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.matching.MatchingEngine;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final MatchingEngine matchingEngine;

    public CvController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
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
}
