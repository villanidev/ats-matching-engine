package com.villanidev.atsmatchingengine.cv.storage;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvGeneratedRepository extends JpaRepository<CvGeneratedEntity, Long> {

    List<CvGeneratedEntity> findByCvMasterId(Long cvMasterId);

    List<CvGeneratedEntity> findByCvMasterIdAndJobPostingId(Long cvMasterId, Long jobPostingId);

    boolean existsByCvMasterIdAndJobPostingId(Long cvMasterId, Long jobPostingId);
}
