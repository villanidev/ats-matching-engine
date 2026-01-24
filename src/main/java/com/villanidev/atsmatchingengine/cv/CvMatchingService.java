package com.villanidev.atsmatchingengine.cv;

import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedEntity;
import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedStoreService;
import com.villanidev.atsmatchingengine.cv.storage.CvMasterStoreService;
import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingNormalizedRepository;
import com.villanidev.atsmatchingengine.matching.JobPostingNormalizedMapper;
import org.springframework.stereotype.Service;

@Service
public class CvMatchingService {

    private final CvGenerator cvGenerator;
    private final CvMasterStoreService cvMasterStoreService;
    private final JobPostingNormalizedRepository jobRepository;
    private final JobPostingNormalizedMapper jobMapper;
    private final CvGeneratedStoreService generatedStoreService;

    public CvMatchingService(
            CvGenerator cvGenerator,
            CvMasterStoreService cvMasterStoreService,
            JobPostingNormalizedRepository jobRepository,
            JobPostingNormalizedMapper jobMapper,
            CvGeneratedStoreService generatedStoreService) {
        this.cvGenerator = cvGenerator;
        this.cvMasterStoreService = cvMasterStoreService;
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
        this.generatedStoreService = generatedStoreService;
    }

    public CvGeneratedEntity generateForJob(Long cvMasterId, Long jobPostingId, Options options) {
        CvMaster cvMaster = cvMasterStoreService.loadCvMaster(cvMasterId)
                .orElseThrow(() -> new IllegalArgumentException("CvMaster not found"));
        JobPostingNormalized normalized = jobRepository.findById(jobPostingId)
                .orElseThrow(() -> new IllegalArgumentException("JobPostingNormalized not found"));

        CvGenerated generated = cvGenerator.generate(cvMaster, jobMapper.toJob(normalized), options);
        Double score = generated.getMeta() != null ? generated.getMeta().getMatchingScoreOverall() : null;
        double threshold = options != null && options.getRelevanceThreshold() != null
                ? options.getRelevanceThreshold()
                : 0.6;
        double resolvedScore = score != null ? score : 0.0;
        if (resolvedScore < threshold) {
            throw new IllegalStateException("Matching score below threshold: " + resolvedScore);
        }

        return generatedStoreService.save(
                cvMasterId,
                jobPostingId,
                normalized.getTitle(),
                normalized.getLocation(),
                score,
                generated
        );
    }
}
