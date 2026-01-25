package com.villanidev.atsmatchingengine.web;

import com.villanidev.atsmatchingengine.api.cv.CvGeneratedSummary;
import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedStoreService;
import com.villanidev.atsmatchingengine.cv.storage.CvMasterEntity;
import com.villanidev.atsmatchingengine.cv.storage.CvMasterStoreService;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class CvMastersWebController {

    private final CvMasterStoreService masterStoreService;
    private final CvGeneratedStoreService generatedStoreService;
    private final ObjectMapper objectMapper;

    public CvMastersWebController(
            CvMasterStoreService masterStoreService,
            CvGeneratedStoreService generatedStoreService,
            ObjectMapper objectMapper) {
        this.masterStoreService = masterStoreService;
        this.generatedStoreService = generatedStoreService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/cv-masters")
    public String list(@RequestParam(value = "limit", defaultValue = "20") int limit, Model model) {
        List<CvMasterEntity> masters = masterStoreService.list(limit);
        model.addAttribute("masters", masters);
        return "cv-masters";
    }

    @GetMapping("/cv-masters/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Optional<CvMasterEntity> entity = masterStoreService.findEntity(id);
        Optional<CvMaster> cvMaster = masterStoreService.loadCvMaster(id);
        if (entity.isEmpty() || cvMaster.isEmpty()) {
            return "redirect:/cv-masters";
        }
        List<CvGeneratedSummary> generated = generatedStoreService.listByCvMaster(id).stream()
                .map(CvGeneratedSummary::new)
                .collect(Collectors.toList());
        model.addAttribute("master", entity.get());
        model.addAttribute("cvMaster", cvMaster.get());
        model.addAttribute("generated", generated);
        model.addAttribute("objectMapper", objectMapper);
        return "cv-master-detail";
    }
}
