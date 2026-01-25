package com.villanidev.atsmatchingengine.elt.scraping;

import com.villanidev.atsmatchingengine.api.elt.PortalConfigRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PortalConfigService {

    private final PortalConfigRepository repository;

    public PortalConfigService(PortalConfigRepository repository) {
        this.repository = repository;
    }

    public List<PortalConfig> list(int limit) {
        int bounded = Math.min(Math.max(limit, 1), 200);
        return repository.findAll(PageRequest.of(0, bounded)).getContent();
    }

    public PortalConfig get(Long id) {
        return repository.findById(id).orElse(null);
    }

    public PortalConfig create(PortalConfigRequest request) {
        PortalConfig entity = mapToEntity(new PortalConfig(), request);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public PortalConfig update(Long id, PortalConfigRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    PortalConfig updated = mapToEntity(existing, request);
                    updated.setUpdatedAt(LocalDateTime.now());
                    return repository.save(updated);
                })
                .orElse(null);
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private PortalConfig mapToEntity(PortalConfig entity, PortalConfigRequest request) {
        entity.setPortalId(request.getPortalId());
        entity.setEnabled(request.isEnabled());
        entity.setBaseUrl(request.getBaseUrl());
        entity.setListingUrl(request.getListingUrl());
        entity.setUserAgent(request.getUserAgent());
        entity.setNotes(request.getNotes());
        entity.setRateLimitMs(request.getRateLimitMs());
        entity.setMaxRetries(request.getMaxRetries());
        return entity;
    }
}
