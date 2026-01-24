package com.villanidev.atsmatchingengine.elt.orchestration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EltExecutionRepository extends JpaRepository<EltExecution, Long> {

	Optional<EltExecution> findTopByStatusOrderByStartedAtDesc(EltExecutionStatus status);

	List<EltExecution> findByStatusOrderByStartedAtDesc(EltExecutionStatus status);

	List<EltExecution> findByStartedAtBetweenOrderByStartedAtDesc(LocalDateTime from, LocalDateTime to);

	List<EltExecution> findByStatusAndStartedAtBetweenOrderByStartedAtDesc(
			EltExecutionStatus status,
			LocalDateTime from,
			LocalDateTime to);
}
