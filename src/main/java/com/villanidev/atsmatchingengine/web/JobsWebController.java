package com.villanidev.atsmatchingengine.web;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingNormalizedRepository;
import com.villanidev.atsmatchingengine.elt.search.JobPostingSearchService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JobsWebController {

    private final JobPostingNormalizedRepository repository;
    private final JobPostingSearchService searchService;

    public JobsWebController(JobPostingNormalizedRepository repository, JobPostingSearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;
    }

    @GetMapping("/jobs")
    public String list(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            Model model) {
        int bounded = Math.min(Math.max(limit, 1), 100);
        List<JobPostingNormalized> results;
        if (query != null && !query.isBlank()) {
            results = searchService.search(query, bounded);
        } else if (source != null && !source.isBlank()) {
            results = repository.findBySourceIgnoreCase(source,
                    PageRequest.of(0, bounded, Sort.by(Sort.Direction.DESC, "normalizedAt"))).getContent();
        } else if (location != null && !location.isBlank()) {
            results = repository.findByLocationContainingIgnoreCase(location,
                    PageRequest.of(0, bounded, Sort.by(Sort.Direction.DESC, "normalizedAt"))).getContent();
        } else if (company != null && !company.isBlank()) {
            results = repository.findByCompanyContainingIgnoreCase(company,
                    PageRequest.of(0, bounded, Sort.by(Sort.Direction.DESC, "normalizedAt"))).getContent();
        } else {
            results = repository.findAll(PageRequest.of(0, bounded, Sort.by(Sort.Direction.DESC, "normalizedAt")))
                    .getContent();
        }
        model.addAttribute("jobs", results);
        model.addAttribute("q", query);
        model.addAttribute("source", source);
        model.addAttribute("location", location);
        model.addAttribute("company", company);
        return "jobs";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        JobPostingNormalized job = repository.findById(id).orElse(null);
        if (job == null) {
            return "redirect:/jobs";
        }
        model.addAttribute("job", job);
        return "job-detail";
    }
}
