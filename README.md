# ATS Matching Engine

A Spring Boot application that implements a CV and job matching engine. The engine analyzes candidate profiles against job requirements and generates tailored CVs with matching scores.

## Features

- REST API endpoint for CV generation based on job requirements
- Intelligent matching algorithm that scores candidates based on:
  - Skills coverage (must-have, nice-to-have, and tools)
  - Domain expertise alignment
  - Years of experience
  - Soft skills match
  - Experience relevance and recency
- Generates structured CV output with relevance scoring
- Markdown output format support

## Prerequisites

- Java 17
- Maven 3.6+

## Building the Application

```bash
mvn clean package
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## API Usage

### POST /api/cv/generate

Generate a tailored CV based on a master CV and job description.

#### Example Request

```bash
curl -X POST http://localhost:8080/api/cv/generate \
  -H "Content-Type: application/json" \
  -d '{
    "cv_master": {
      "name": "John Doe",
      "title": "Senior Software Engineer",
      "email": "john.doe@example.com",
      "location": "New York, USA",
      "phone": "+1-555-0123",
      "linkedin": "https://linkedin.com/in/johndoe",
      "summary": [
        "Experienced software engineer with 10 years in backend development",
        "Strong expertise in Java and Spring ecosystem"
      ],
      "skills": [
        {
          "name": "Java",
          "level": "expert"
        },
        {
          "name": "Spring Boot",
          "level": "advanced"
        },
        {
          "name": "PostgreSQL",
          "level": "advanced"
        }
      ],
      "domains": ["Finance", "E-commerce"],
      "experiences": [
        {
          "company": "Tech Corp",
          "country": "USA",
          "title": "Senior Engineer",
          "start": "2020-01",
          "end": "present",
          "projects": [
            {
              "name": "Payment System",
              "period": "2020-2023",
              "situation": "Legacy system needed modernization",
              "task": "Redesign payment processing",
              "actions": [
                "Implemented microservices architecture",
                "Added Redis caching layer"
              ],
              "result": "Reduced processing time by 50%",
              "tech_stack": ["Java", "Spring Boot", "Redis", "PostgreSQL"],
              "team_size": "5",
              "domains": ["Finance"]
            }
          ]
        }
      ],
      "education": [
        {
          "degree": "BSc Computer Science",
          "institution": "University of Technology",
          "country": "USA",
          "start": "2010",
          "end": "2014"
        }
      ],
      "languages": [
        {
          "name": "English",
          "level": "native"
        }
      ]
    },
    "job": {
      "id": "job-123",
      "title": "Senior Backend Engineer",
      "location": "Remote",
      "seniority": "senior",
      "raw_description": "Looking for an experienced backend engineer...",
      "requirements": {
        "years_of_experience": 5,
        "must_have_skills": ["Java", "Spring Boot"],
        "nice_to_have_skills": ["Redis", "PostgreSQL"],
        "tools": ["Git", "Docker"],
        "domains": ["Finance"]
      },
      "responsibilities": [
        "Design scalable systems",
        "Mentor junior developers"
      ],
      "soft_skills": ["leadership", "communication"]
    },
    "options": {
      "language": "en",
      "output_formats": ["structured", "markdown"]
    }
  }'
```

#### Example Response

The response includes:
- `cv_generated.meta`: Job information and matching scores
  - `matching_score_overall`: Overall compatibility score (0-1)
  - `matching_details`: Breakdown of skills coverage, domain fit, and experience relevance
- `cv_generated.header`: Candidate contact information
- `cv_generated.summary`: Professional summary
- `cv_generated.skills_section`: Highlighted skills matching the job
- `cv_generated.experience_section`: Work experiences sorted by relevance with individual relevance scores
- `cv_generated.education_section`: Educational background
- `cv_generated.languages_section`: Language proficiencies
- `cv_generated.output`: Generated markdown and HTML output

## Running Tests

```bash
mvn test
```

## Project Structure

```
src/
├── main/
│   ├── java/com/villanidev/atsmatchingengine/
│   │   ├── AtsMatchingEngineApplication.java   # Main Spring Boot application
│   │   ├── api/                                # REST API controllers and DTOs
│   │   ├── domain/                             # Domain models
│   │   └── matching/                           # Matching engine logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/villanidev/atsmatchingengine/
        └── matching/                           # Unit tests
```

## Matching Algorithm

The engine uses a weighted scoring system:

1. **Global Skill Score (45%)**: Coverage of must-have skills (60%), nice-to-have skills (25%), and tools (15%)
2. **Experience Relevance (20%)**: Average of top 3 most relevant work experiences
3. **Domain Fit (10%)**: Alignment between candidate and job domains
4. **Experience Years (10%)**: Years of experience vs. job requirements
5. **Soft Skills (15%)**: Presence of required soft skills in candidate profile

Each experience is also scored individually based on:
- Skills overlap (60%)
- Domain match (20%)
- Recency (20%) - linear decay from 1.0 (current) to 0.2 (5+ years old)