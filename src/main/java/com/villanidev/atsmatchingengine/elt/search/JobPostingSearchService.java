package com.villanidev.atsmatchingengine.elt.search;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingNormalizedRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JobPostingSearchService {

    private final JobPostingNormalizedRepository repository;

    public JobPostingSearchService(JobPostingNormalizedRepository repository) {
        this.repository = repository;
    }

    public List<JobPostingNormalized> search(String queryText, int limit) {
        if (queryText == null || queryText.isBlank()) {
            return List.of();
        }
        return repository.searchFullText(queryText, limit);
    }
}
