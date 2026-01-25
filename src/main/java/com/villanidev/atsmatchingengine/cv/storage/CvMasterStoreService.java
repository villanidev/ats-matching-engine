package com.villanidev.atsmatchingengine.cv.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    public Optional<CvMasterEntity> update(Long id, CvMaster cvMaster) {
        return repository.findById(id).map(existing -> {
            existing.setName(cvMaster.getName());
            existing.setTitle(cvMaster.getTitle());
            existing.setEmail(cvMaster.getEmail());
            existing.setPayloadJson(toJson(cvMaster));
            existing.setUpdatedAt(LocalDateTime.now());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    public List<CvMasterEntity> list(int limit) {
        int bounded = Math.min(Math.max(limit, 1), 100);
        return repository.findAll(org.springframework.data.domain.PageRequest.of(0, bounded)).getContent();
    }

    public Optional<CvMaster> loadCvMaster(Long id) {
        return repository.findById(id).map(entity -> fromJson(entity.getPayloadJson()));
    }

    public Optional<CvMasterEntity> findEntity(Long id) {
        return repository.findById(id);
    }

    public List<Long> listIds() {
        return repository.findAll().stream().map(CvMasterEntity::getId).collect(Collectors.toList());
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
