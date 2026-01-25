package com.villanidev.atsmatchingengine.cv;

import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedRepository;
import com.villanidev.atsmatchingengine.cv.storage.CvGeneratedEntity;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.elt.model.JobPostingNormalized;
import com.villanidev.atsmatchingengine.elt.repository.JobPostingNormalizedRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CvBatchMatchingService {

    private final CvMatchingService matchingService;
    private final JobPostingNormalizedRepository jobRepository;
    private final CvGeneratedRepository generatedRepository;

    public CvBatchMatchingService(
            CvMatchingService matchingService,
            JobPostingNormalizedRepository jobRepository,
            CvGeneratedRepository generatedRepository) {
        this.matchingService = matchingService;
        this.jobRepository = jobRepository;
        this.generatedRepository = generatedRepository;
    }

    public BatchResult runBatch(Long cvMasterId, Options options, int limit) {
        int bounded = Math.min(Math.max(limit, 1), 500);
        List<JobPostingNormalized> jobs = jobRepository.findAll(PageRequest.of(0, bounded)).getContent();
        int generated = 0;
        int skippedExisting = 0;
        int skippedBelowThreshold = 0;
        int failed = 0;
        List<Long> generatedIds = new ArrayList<>();

        for (JobPostingNormalized job : jobs) {
            if (generatedRepository.existsByCvMasterIdAndJobPostingId(cvMasterId, job.getId())) {
                skippedExisting++;
                continue;
            }
            try {
                CvGeneratedEntity entity = matchingService.generateForJob(cvMasterId, job.getId(), options);
                generated++;
                generatedIds.add(entity.getId());
            } catch (IllegalStateException ex) {
                skippedBelowThreshold++;
            } catch (Exception ex) {
                failed++;
            }
        }

        return new BatchResult(jobs.size(), generated, skippedExisting, skippedBelowThreshold, failed, generatedIds);
    }

    public static class BatchResult {
        private final int totalJobs;
        private final int generated;
        private final int skippedExisting;
        private final int skippedBelowThreshold;
        private final int failed;
        private final List<Long> generatedIds;

        public BatchResult(int totalJobs,
                           int generated,
                           int skippedExisting,
                           int skippedBelowThreshold,
                           int failed,
                           List<Long> generatedIds) {
            this.totalJobs = totalJobs;
            this.generated = generated;
            this.skippedExisting = skippedExisting;
            this.skippedBelowThreshold = skippedBelowThreshold;
            this.failed = failed;
            this.generatedIds = generatedIds;
        }

        public int getTotalJobs() {
            return totalJobs;
        }

        public int getGenerated() {
            return generated;
        }

        public int getSkippedExisting() {
            return skippedExisting;
        }

        public int getSkippedBelowThreshold() {
            return skippedBelowThreshold;
        }

        public int getFailed() {
            return failed;
        }

        public List<Long> getGeneratedIds() {
            return generatedIds;
        }
    }
}
