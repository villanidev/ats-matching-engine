package com.villanidev.atsmatchingengine.matching;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.Options;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchingEngine {

        private final MatchingScoringService scoringService;
        private final CvSectionsBuilder sectionsBuilder;
        private final OutputRenderer outputRenderer;

        public MatchingEngine() {
                this(new MatchingScoringService(), new CvSectionsBuilder(), new OutputRenderer());
        }

        public MatchingEngine(MatchingScoringService scoringService,
                                                  CvSectionsBuilder sectionsBuilder,
                                                  OutputRenderer outputRenderer) {
                this.scoringService = scoringService;
                this.sectionsBuilder = sectionsBuilder;
                this.outputRenderer = outputRenderer;
        }

        public CvGenerated generateCv(CvMaster cvMaster, Job job, Options options) {
                CvGenerated cvGenerated = new CvGenerated();

                CvGenerated.Meta meta = scoringService.buildMeta(cvMaster, job);
                cvGenerated.setMeta(meta);

                CvGenerated.Header header = sectionsBuilder.buildHeader(cvMaster);
                cvGenerated.setHeader(header);

                cvGenerated.setSummary(cvMaster.getSummary() != null ? cvMaster.getSummary() : List.of());

                CvGenerated.SkillsSection skillsSection = sectionsBuilder.buildSkillsSection(cvMaster, job);
                cvGenerated.setSkillsSection(skillsSection);

                List<CvGenerated.ExperienceSection> experienceSection = sectionsBuilder.buildExperienceSection(
                                cvMaster,
                                job,
                        options,
                                scoringService.getExperienceScorer()
                );
                cvGenerated.setExperienceSection(experienceSection);

                List<CvGenerated.EducationSection> educationSection = sectionsBuilder.buildEducationSection(cvMaster);
                cvGenerated.setEducationSection(educationSection);

                List<CvGenerated.LanguageSection> languagesSection = sectionsBuilder.buildLanguagesSection(cvMaster);
                cvGenerated.setLanguagesSection(languagesSection);

                CvGenerated.Output output = outputRenderer.buildOutput(cvGenerated);
                cvGenerated.setOutput(output);

                return cvGenerated;
        }
}
