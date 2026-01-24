package com.villanidev.atsmatchingengine.elt.scraping;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortalConfigRepository extends JpaRepository<PortalConfig, Long> {

    List<PortalConfig> findByEnabledTrue();

    Optional<PortalConfig> findByPortalIdIgnoreCase(String portalId);
}
