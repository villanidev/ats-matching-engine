package com.villanidev.atsmatchingengine.elt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elt.scheduler")
public class EltSchedulerProperties {

    /**
     * Enables or disables the scheduled ELT job.
     */
    private boolean enabled = true;

    /**
     * Cron expression for the scheduled ELT job.
     */
    private String cron = "0 0 2 * * *";

    /**
     * Timezone for cron evaluation.
     */
    private String timezone = "UTC";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
