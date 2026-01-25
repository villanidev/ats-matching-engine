package com.villanidev.atsmatchingengine.web;

import com.villanidev.atsmatchingengine.elt.orchestration.EltExecution;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionItemRepository;
import com.villanidev.atsmatchingengine.elt.orchestration.EltExecutionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EltExecutionRepository executionRepository;
    private final EltExecutionItemRepository itemRepository;
    private final int executionsLimit;

    public DashboardController(
            EltExecutionRepository executionRepository,
            EltExecutionItemRepository itemRepository,
            @Value("${app.dashboard.executions.limit:10}") int executionsLimit) {
        this.executionRepository = executionRepository;
        this.itemRepository = itemRepository;
        this.executionsLimit = executionsLimit;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<EltExecution> executions = executionRepository.findAll(
                PageRequest.of(0, Math.min(Math.max(executionsLimit, 1), 50), Sort.by(Sort.Direction.DESC, "startedAt")))
                .getContent();
        model.addAttribute("executions", executions);
        model.addAttribute("itemsByExecution", executions.stream().collect(java.util.stream.Collectors.toMap(
                EltExecution::getId,
                exec -> itemRepository.findByExecutionId(exec.getId())
        )));
        return "dashboard";
    }
}
