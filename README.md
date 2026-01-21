# ATS Matching Engine

Spring Boot service that parses CVs, matches them against a job description, and generates tailored CV output (Markdown/PDF/DOCX) with matching scores.

## Features

- JSON CV generation and multipart upload parsing (PDF/DOCX)
- Scoring: skills coverage, domain fit, experience relevance, experience years, soft skills
- Text relevance via Lucene (BM25 or TF‑IDF)
- Output: Markdown + base64 PDF/DOCX, plus binary download endpoints
- Template selection by profile

## Requirements

- Java 21
- Maven 3.6+

## Run

```bash
mvn spring-boot:run
```

Server: http://localhost:8080

## API

### JSON

POST /api/cv/generate

Options (partial):

- `output_formats`: `pdf`, `docx` (defaults to both)
- `profile`: template profile key
- `text_relevance_strategy`: `bm25` (default) | `tfidf` | `none`

### Upload

POST /api/cv/generate-upload (multipart)
POST /api/cv/generate-upload/pdf
POST /api/cv/generate-upload/docx

## Example (JSON)

```bash
curl -X POST http://localhost:8080/api/cv/generate \
  -H "Content-Type: application/json" \
  -d '{
    "cv_master": { "name": "Jane Doe", "email": "jane@dev.com" },
    "job": { "id": "job-1", "title": "Backend Engineer", "requirements": {"must_have_skills": ["Java"]} },
    "options": { "text_relevance_strategy": "tfidf" }
  }'
```

## Project Structure

```
src/main/java/com/villanidev/atsmatchingengine
├── api
│   ├── cv        # CV endpoints + DTOs
│   ├── debug     # Debug endpoints
│   └── error     # Error handling
├── cv            # CV generation orchestration
├── domain        # Domain models
├── matching
│   ├── scoring   # Scoring strategies (BM25/TF‑IDF)
│   └── sections  # CV section builders
├── parsing       # Upload parsing (Tika)
├── rendering     # Markdown/PDF/DOCX rendering
├── shared        # Normalizers + helpers
└── templates     # CV templates
```
