package com.villanidev.atsmatchingengine.matching.scoring;

import com.villanidev.atsmatchingengine.domain.CvMaster;
import com.villanidev.atsmatchingengine.domain.Job;
import com.villanidev.atsmatchingengine.domain.TextRelevanceStrategy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.util.List;

public class TextRelevanceScorer {

    public double computeTextRelevanceScore(CvMaster cvMaster, Job job, TextRelevanceStrategy strategy) {
        if (strategy == TextRelevanceStrategy.NONE) {
            return 0.0;
        }

        TextRelevanceStrategy resolved = strategy != null ? strategy : TextRelevanceStrategy.BM25;
        String candidateText = buildCandidateText(cvMaster);
        String jobText = buildJobText(job);

        if (candidateText.isBlank() || jobText.isBlank()) {
            return 0.0;
        }

        try (Analyzer analyzer = new StandardAnalyzer();
             Directory directory = new ByteBuffersDirectory()) {
            Similarity similarity = similarityFor(resolved);
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setSimilarity(similarity);

            try (IndexWriter writer = new IndexWriter(directory, config)) {
                Document document = new Document();
                document.add(new TextField("content", candidateText, Field.Store.NO));
                writer.addDocument(document);
                writer.commit();
            }

            try (DirectoryReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                searcher.setSimilarity(similarity);

                QueryParser parser = new QueryParser("content", analyzer);
                parser.setDefaultOperator(QueryParser.Operator.OR);
                Query query = parser.parse(QueryParserBase.escape(jobText));

                ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
                if (hits.length == 0) {
                    return 0.0;
                }

                float rawScore = hits[0].score;
                return normalizeScore(rawScore);
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String buildCandidateText(CvMaster cvMaster) {
        StringBuilder builder = new StringBuilder();

        appendAll(builder, cvMaster.getSummary());
        appendAll(builder, cvMaster.getSkills());
        appendAll(builder, cvMaster.getDomains());

        if (cvMaster.getExperiences() != null) {
            cvMaster.getExperiences().forEach(exp -> {
                append(builder, exp.getTitle());
                append(builder, exp.getCompany());
                if (exp.getProjects() != null) {
                    exp.getProjects().forEach(project -> {
                        append(builder, project.getSituation());
                        append(builder, project.getTask());
                        append(builder, project.getResult());
                        appendAll(builder, project.getActions());
                        appendAll(builder, project.getTechStack());
                        appendAll(builder, project.getDomains());
                    });
                }
            });
        }

        if (cvMaster.getEducation() != null) {
            cvMaster.getEducation().forEach(edu -> {
                append(builder, edu.getDegree());
                append(builder, edu.getInstitution());
            });
        }

        return builder.toString().trim();
    }

    private String buildJobText(Job job) {
        StringBuilder builder = new StringBuilder();

        append(builder, job.getTitle());
        append(builder, job.getRawDescription());
        appendAll(builder, job.getResponsibilities());
        appendAll(builder, job.getSoftSkills());

        if (job.getRequirements() != null) {
            appendAll(builder, job.getRequirements().getMustHaveSkills());
            appendAll(builder, job.getRequirements().getNiceToHaveSkills());
            appendAll(builder, job.getRequirements().getTools());
            appendAll(builder, job.getRequirements().getMethodologies());
            appendAll(builder, job.getRequirements().getDomains());
            if (job.getRequirements().getLanguages() != null) {
                job.getRequirements().getLanguages().forEach(lang -> {
                    append(builder, lang.getName());
                    append(builder, lang.getLevel());
                });
            }
        }

        return builder.toString().trim();
    }

    private void appendAll(StringBuilder builder, List<?> values) {
        if (values == null) {
            return;
        }
        for (Object value : values) {
            append(builder, value);
        }
    }

    private void append(StringBuilder builder, Object value) {
        if (value == null) {
            return;
        }
        String text;
        if (value instanceof CvMaster.Skill skill) {
            text = skill.getName();
        } else {
            text = value.toString();
        }
        if (text == null || text.isBlank()) {
            return;
        }
        builder.append(text).append(' ');
    }

    private double normalizeScore(float rawScore) {
        if (rawScore <= 0f) {
            return 0.0;
        }
        return 1.0 - Math.exp(-rawScore);
    }

    private Similarity similarityFor(TextRelevanceStrategy strategy) {
        if (strategy == TextRelevanceStrategy.TFIDF) {
            return new ClassicSimilarity();
        }
        return new BM25Similarity();
    }
}