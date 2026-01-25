package com.villanidev.atsmatchingengine.web;

import com.villanidev.atsmatchingengine.elt.scraping.PortalConfig;
import com.villanidev.atsmatchingengine.elt.scraping.PortalConfigService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PortalConfigWebController {

    private final PortalConfigService service;

    public PortalConfigWebController(PortalConfigService service) {
        this.service = service;
    }

    @GetMapping("/portals")
    public String list(@RequestParam(value = "limit", defaultValue = "50") int limit,
                       @RequestParam(value = "success", required = false) String success,
                       @RequestParam(value = "error", required = false) String error,
                       Model model) {
        List<PortalConfig> portals = service.list(limit);
        model.addAttribute("portals", portals);
        model.addAttribute("success", success);
        model.addAttribute("error", error);
        model.addAttribute("form", new com.villanidev.atsmatchingengine.api.elt.PortalConfigRequest());
        return "portals";
    }

    @GetMapping("/portals/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        PortalConfig portal = service.get(id);
        if (portal == null) {
            return "redirect:/portals";
        }
        model.addAttribute("portal", portal);
        model.addAttribute("success", null);
        model.addAttribute("error", null);
        return "portal-detail";
    }

    @PostMapping("/portals")
    public String create(
            @RequestParam("portalId") String portalId,
            @RequestParam(value = "enabled", defaultValue = "true") boolean enabled,
            @RequestParam(value = "baseUrl", required = false) String baseUrl,
            @RequestParam(value = "listingUrl", required = false) String listingUrl,
            @RequestParam(value = "userAgent", required = false) String userAgent,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "rateLimitMs", required = false) Integer rateLimitMs,
            @RequestParam(value = "maxRetries", required = false) Integer maxRetries) {
        com.villanidev.atsmatchingengine.api.elt.PortalConfigRequest request =
                new com.villanidev.atsmatchingengine.api.elt.PortalConfigRequest();
        request.setPortalId(portalId);
        request.setEnabled(enabled);
        request.setBaseUrl(baseUrl);
        request.setListingUrl(listingUrl);
        request.setUserAgent(userAgent);
        request.setNotes(notes);
        request.setRateLimitMs(rateLimitMs);
        request.setMaxRetries(maxRetries);
        Optional<String> validationError = validate(request);
        if (validationError.isPresent()) {
            return "redirect:/portals?error=" + validationError.get();
        }
        service.create(request);
        return "redirect:/portals?success=created";
    }

    @PostMapping("/portals/{id}/update")
    public String update(
            @PathVariable("id") Long id,
            @RequestParam("portalId") String portalId,
            @RequestParam(value = "enabled", defaultValue = "true") boolean enabled,
            @RequestParam(value = "baseUrl", required = false) String baseUrl,
            @RequestParam(value = "listingUrl", required = false) String listingUrl,
            @RequestParam(value = "userAgent", required = false) String userAgent,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "rateLimitMs", required = false) Integer rateLimitMs,
            @RequestParam(value = "maxRetries", required = false) Integer maxRetries) {
        com.villanidev.atsmatchingengine.api.elt.PortalConfigRequest request =
                new com.villanidev.atsmatchingengine.api.elt.PortalConfigRequest();
        request.setPortalId(portalId);
        request.setEnabled(enabled);
        request.setBaseUrl(baseUrl);
        request.setListingUrl(listingUrl);
        request.setUserAgent(userAgent);
        request.setNotes(notes);
        request.setRateLimitMs(rateLimitMs);
        request.setMaxRetries(maxRetries);
        Optional<String> validationError = validate(request);
        if (validationError.isPresent()) {
            return "redirect:/portals/" + id + "?error=" + validationError.get();
        }
        service.update(id, request);
        return "redirect:/portals/" + id + "?success=updated";
    }

    @PostMapping("/portals/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        service.delete(id);
        return "redirect:/portals?success=deleted";
    }

    private Optional<String> validate(com.villanidev.atsmatchingengine.api.elt.PortalConfigRequest request) {
        if (request.getPortalId() == null || request.getPortalId().isBlank()) {
            return Optional.of("portalId_required");
        }
        if (request.getListingUrl() == null || request.getListingUrl().isBlank()) {
            return Optional.of("listingUrl_required");
        }
        return Optional.empty();
    }
}
