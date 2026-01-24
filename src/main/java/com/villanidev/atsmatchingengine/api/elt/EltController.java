package com.villanidev.atsmatchingengine.api.elt;

import com.villanidev.atsmatchingengine.elt.EltPipelineService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elt")
public class EltController {

    private final EltPipelineService pipelineService;

    public EltController(EltPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runOnDemand(
            @RequestParam(value = "sourceId", required = false) String sourceId) {
        Map<String, Object> response = new HashMap<>();
        if (StringUtils.hasText(sourceId)) {
            pipelineService.runOnDemand(sourceId);
            response.put("scope", "source");
            response.put("sourceId", sourceId);
        } else {
            pipelineService.runOnDemandAll();
            response.put("scope", "all");
        }
        response.put("status", "started");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
