package com.villanidev.atsmatchingengine.api;

import com.villanidev.atsmatchingengine.parsing.CvUploadParser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final CvUploadParser cvUploadParser;

    public DebugController(CvUploadParser cvUploadParser) {
        this.cvUploadParser = cvUploadParser;
    }

    @PostMapping(value = "/parse-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> parseDocument(@RequestPart("file") MultipartFile file) {
        String text = cvUploadParser.extractTextFromFile(file);
        return ResponseEntity.ok(text);
    }
}
