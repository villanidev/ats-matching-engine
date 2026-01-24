package com.villanidev.atsmatchingengine.elt.scraping;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elt.portals")
public class EltPortalProperties {

    private List<PortalConfig> configs = new ArrayList<>();

    public List<PortalConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<PortalConfig> configs) {
        this.configs = configs;
    }
}
