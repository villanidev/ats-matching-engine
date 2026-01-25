package com.villanidev.atsmatchingengine.api.elt;

import com.villanidev.atsmatchingengine.elt.scraping.PortalConfig;
import com.villanidev.atsmatchingengine.elt.scraping.PortalConfigService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elt/portals")
public class PortalConfigController {

    private final PortalConfigService service;

    public PortalConfigController(PortalConfigService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PortalConfigResponse>> list(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        List<PortalConfigResponse> response = service.list(limit).stream()
            .map(PortalConfigResponse::new)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortalConfigResponse> get(@PathVariable("id") Long id) {
        PortalConfig entity = service.get(id);
        return entity == null
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(new PortalConfigResponse(entity));
    }

    @PostMapping
    public ResponseEntity<PortalConfigResponse> create(@Valid @RequestBody PortalConfigRequest request) {
        return ResponseEntity.ok(new PortalConfigResponse(service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortalConfigResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody PortalConfigRequest request) {
        PortalConfig updated = service.update(id, request);
        return updated == null
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(new PortalConfigResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
