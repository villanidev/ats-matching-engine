package com.villanidev.atsmatchingengine.cv.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CvMasterStoreService {

    private final CvMasterRepository repository;
    private final ObjectMapper objectMapper;

    public CvMasterStoreService(CvMasterRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public CvMasterEntity save(CvMaster cvMaster) {
        CvMasterEntity entity = new CvMasterEntity();
        entity.setName(cvMaster.getName());
        entity.setTitle(cvMaster.getTitle());
        entity.setEmail(cvMaster.getEmail());
        entity.setPayloadJson(toJson(cvMaster));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public Optional<CvMaster> loadCvMaster(Long id) {
        return repository.findById(id).map(entity -> fromJson(entity.getPayloadJson()));
    }

    public Optional<CvMasterEntity> findEntity(Long id) {
        return repository.findById(id);
    }

    private String toJson(CvMaster cvMaster) {
        try {
            return objectMapper.writeValueAsString(cvMaster);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize CvMaster", ex);
        }
    }

    private CvMaster fromJson(String json) {
        try {
            return objectMapper.readValue(json, CvMaster.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize CvMaster", ex);
        }
    }
}
