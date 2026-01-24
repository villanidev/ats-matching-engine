package com.villanidev.atsmatchingengine.api.elt;

import com.villanidev.atsmatchingengine.elt.orchestration.EltExecution;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionItem;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionItemRepository;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionRepository;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elt")
public class EltMonitoringController {

    private final EltExecutionRepository executionRepository;
    private final EltExecutionItemRepository itemRepository;

    public EltMonitoringController(
            EltExecutionRepository executionRepository,
            EltExecutionItemRepository itemRepository) {
        this.executionRepository = executionRepository;
        this.itemRepository = itemRepository;
    }

    @GetMapping("/executions")
    public ResponseEntity<List<EltExecution>> listExecutions(
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "status", required = false) EltExecutionStatus status,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<EltExecution> executions;
        if (from != null && to != null && status != null) {
            executions = executionRepository.findByStatusAndStartedAtBetweenOrderByStartedAtDesc(status, from, to);
        } else if (from != null && to != null) {
            executions = executionRepository.findByStartedAtBetweenOrderByStartedAtDesc(from, to);
        } else if (status != null) {
            executions = executionRepository.findByStatusOrderByStartedAtDesc(status);
        } else {
            executions = executionRepository.findAll(
                    PageRequest.of(0, Math.min(Math.max(limit, 1), 100), Sort.by(Sort.Direction.DESC, "startedAt")))
                    .getContent();
        }
        if (executions.size() > Math.min(Math.max(limit, 1), 100)) {
            executions = executions.subList(0, Math.min(Math.max(limit, 1), 100));
        }
        return ResponseEntity.ok(executions);
    }

    @GetMapping("/executions/active")
    public ResponseEntity<EltExecution> getActiveExecution() {
        return executionRepository.findTopByStatusOrderByStartedAtDesc(EltExecutionStatus.RUNNING)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/executions/{id}")
    public ResponseEntity<Map<String, Object>> getExecution(@PathVariable("id") Long id) {
        EltExecution execution = executionRepository.findById(id).orElse(null);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        List<EltExecutionItem> items = itemRepository.findByExecutionId(id);
        Map<String, Object> response = new HashMap<>();
        response.put("execution", execution);
        response.put("items", items);
        return ResponseEntity.ok(response);
    }
}
