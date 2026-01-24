package com.villanidev.atsmatchingengine.elt.search;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresSearchVectorInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PostgresSearchVectorInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public PostgresSearchVectorInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isPostgres()) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE job_posting_normalized ADD COLUMN IF NOT EXISTS search_vector tsvector");
            jdbcTemplate.execute(
                    "CREATE INDEX IF NOT EXISTS idx_job_posting_normalized_search_vector "
                            + "ON job_posting_normalized USING GIN (search_vector)");
            jdbcTemplate.execute(
                    "CREATE OR REPLACE FUNCTION job_posting_normalized_tsvector_update() "
                            + "RETURNS trigger AS $$ "
                            + "BEGIN "
                            + "NEW.search_vector := to_tsvector('simple', "
                            + "coalesce(NEW.title,'') || ' ' || "
                            + "coalesce(NEW.description,'') || ' ' || "
                            + "coalesce(NEW.requirements,'') || ' ' || "
                            + "coalesce(NEW.company,'') || ' ' || "
                            + "coalesce(NEW.location,'')); "
                            + "RETURN NEW; "
                            + "END $$ LANGUAGE plpgsql");
            jdbcTemplate.execute(
                    "DO $$ BEGIN "
                            + "IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'job_posting_normalized_tsvector_trigger') THEN "
                            + "CREATE TRIGGER job_posting_normalized_tsvector_trigger "
                            + "BEFORE INSERT OR UPDATE ON job_posting_normalized "
                            + "FOR EACH ROW EXECUTE FUNCTION job_posting_normalized_tsvector_update(); "
                            + "END IF; "
                            + "END $$");
            jdbcTemplate.execute(
                    "UPDATE job_posting_normalized SET search_vector = to_tsvector('simple', "
                            + "coalesce(title,'') || ' ' || "
                            + "coalesce(description,'') || ' ' || "
                            + "coalesce(requirements,'') || ' ' || "
                            + "coalesce(company,'') || ' ' || "
                            + "coalesce(location,''))");
        } catch (Exception ex) {
            logger.info("Failed to initialize PostgreSQL search vector. message={}", ex.getMessage());
        }
    }

    private boolean isPostgres() {
        try {
            String product = dataSource.getConnection().getMetaData().getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgres");
        } catch (Exception ex) {
            return false;
        }
    }
}
