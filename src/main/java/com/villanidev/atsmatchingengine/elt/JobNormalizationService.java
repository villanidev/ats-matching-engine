package com.villanidev.atsmatchingengine.elt;

import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.model.JobPostingRaw;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobNormalizationService {

    private static final Logger logger = LoggerFactory.getLogger(JobNormalizationService.class);

    public Optional<JobPostingNormalized> normalize(JobPostingRaw raw) {
        if (raw == null || raw.getRawContent() == null || raw.getRawContent().isBlank()) {
            return Optional.empty();
        }
        JobPostingNormalized normalized = new JobPostingNormalized();
        normalized.setRawId(raw.getId());
        normalized.setSource(raw.getSource());
        normalized.setExternalId(raw.getExternalId());
        normalized.setNormalizedAt(LocalDateTime.now());
        logger.debug("Normalized RAW job id={} source={} externalId={}", raw.getId(), raw.getSource(), raw.getExternalId());
        return Optional.of(normalized);
    }
}
