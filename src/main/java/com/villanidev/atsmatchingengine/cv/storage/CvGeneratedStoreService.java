package com.villanidev.atsmatchingengine.cv.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.domain.CvGenerated;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CvGeneratedStoreService {

    private final CvGeneratedRepository repository;
    private final ObjectMapper objectMapper;

    public CvGeneratedStoreService(CvGeneratedRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public CvGeneratedEntity save(Long cvMasterId,
                                  Long jobPostingId,
                                  String jobTitle,
                                  String jobLocation,
                                  Double matchingScore,
                                  CvGenerated cvGenerated) {
        CvGeneratedEntity entity = new CvGeneratedEntity();
        entity.setCvMasterId(cvMasterId);
        entity.setJobPostingId(jobPostingId);
        entity.setJobTitle(jobTitle);
        entity.setJobLocation(jobLocation);
        entity.setMatchingScore(matchingScore);
        entity.setPayloadJson(toJson(cvGenerated));
        entity.setCreatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public List<CvGeneratedEntity> listByCvMaster(Long cvMasterId) {
        return repository.findByCvMasterId(cvMasterId);
    }

    public List<CvGeneratedEntity> listByCvMasterAndJob(Long cvMasterId, Long jobPostingId) {
        return repository.findByCvMasterIdAndJobPostingId(cvMasterId, jobPostingId);
    }

    private String toJson(CvGenerated cvGenerated) {
        try {
            return objectMapper.writeValueAsString(cvGenerated);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize CvGenerated", ex);
        }
    }
}
