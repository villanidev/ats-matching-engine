package com.villanidev.atsmatchingengine.elt.orchestration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EltExecutionItemRepository extends JpaRepository<EltExecutionItem, Long> {

    List<EltExecutionItem> findByExecutionId(Long executionId);
}
