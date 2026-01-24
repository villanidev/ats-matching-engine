package com.villanidev.atsmatchingengine.elt.repository;

import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRawRepository extends JpaRepository<JobPostingRaw, Long> {

    List<JobPostingRaw> findByNormalizedFalse();

    boolean existsBySourceAndExternalId(String source, String externalId);

    boolean existsByUrl(String url);
}
