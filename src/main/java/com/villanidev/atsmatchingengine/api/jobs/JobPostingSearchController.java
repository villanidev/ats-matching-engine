package com.villanidev.atsmatchingengine.api.jobs;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.search.JobPostingSearchService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingSearchController {

    private final JobPostingSearchService searchService;

    public JobPostingSearchController(JobPostingSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobPostingNormalized>> search(
            @RequestParam("q") String queryText,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        int bounded = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(searchService.search(queryText, bounded));
    }
}
