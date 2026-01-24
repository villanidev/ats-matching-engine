package com.villanidev.atsmatchingengine.elt.orchestration;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class EltExecutionService {

    private final EltExecutionRepository executionRepository;
    private final EltExecutionItemRepository itemRepository;

    public EltExecutionService(
            EltExecutionRepository executionRepository,
            EltExecutionItemRepository itemRepository) {
        this.executionRepository = executionRepository;
        this.itemRepository = itemRepository;
    }

    public EltExecution startExecution(String triggerType, String scope) {
        EltExecution execution = new EltExecution();
        execution.setTriggerType(triggerType);
        execution.setScope(scope);
        execution.setStatus(EltExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());
        return executionRepository.save(execution);
    }

    public EltExecutionItem startItem(EltExecution execution, String portalId) {
        EltExecutionItem item = new EltExecutionItem();
        item.setExecution(execution);
        item.setPortalId(portalId);
        item.setStatus(EltExecutionItemStatus.RUNNING);
        item.setStartedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }

    public void completeItem(EltExecutionItem item, EltExecutionItemStatus status, Integer itemCount, String message) {
        item.setStatus(status);
        item.setItemCount(itemCount);
        item.setMessage(message);
        item.setFinishedAt(LocalDateTime.now());
        itemRepository.save(item);
    }

    public void finishExecution(EltExecution execution, EltExecutionStatus status, String message) {
        execution.setStatus(status);
        execution.setMessage(message);
        execution.setFinishedAt(LocalDateTime.now());
        executionRepository.save(execution);
    }
}
