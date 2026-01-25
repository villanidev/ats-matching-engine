package com.villanidev.atsmatchingengine.api.jobs;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingNormalizedRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingController {

    private final JobPostingNormalizedRepository repository;

    public JobPostingController(JobPostingNormalizedRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<JobPostingSummary>> list(
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "company", required = false) String company) {
        int bounded = Math.min(Math.max(limit, 1), 100);
        PageRequest page = PageRequest.of(0, bounded, Sort.by(Sort.Direction.DESC, "normalizedAt"));
        List<JobPostingNormalized> results;
        if (source != null && !source.isBlank()) {
            results = repository.findBySourceIgnoreCase(source, page).getContent();
        } else if (location != null && !location.isBlank()) {
            results = repository.findByLocationContainingIgnoreCase(location, page).getContent();
        } else if (company != null && !company.isBlank()) {
            results = repository.findByCompanyContainingIgnoreCase(company, page).getContent();
        } else {
            results = repository.findAll(page).getContent();
        }
        List<JobPostingSummary> response = results.stream().map(JobPostingSummary::new).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingNormalized> getById(@PathVariable("id") Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
