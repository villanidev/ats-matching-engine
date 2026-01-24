package com.villanidev.atsmatchingengine.elt.repository;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobPostingNormalizedRepository extends JpaRepository<JobPostingNormalized, Long> {

	@Query(value = "SELECT * FROM job_posting_normalized "
			+ "WHERE search_vector @@ plainto_tsquery('simple', :query) "
			+ "ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :query)) DESC "
			+ "LIMIT :limit", nativeQuery = true)
	List<JobPostingNormalized> searchFullText(@Param("query") String query, @Param("limit") int limit);
}
