package com.villanidev.atsmatchingengine.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CvControllerUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGenerateCvFromUpload_withTextFile() throws Exception {
        String cvContent = "John Doe\nSenior Software Engineer with 10 years of experience.\nExpert in Java and Spring Boot.";
        String jobDescription = "Senior Backend Engineer\nWe are looking for an experienced Java developer.";

        MockMultipartFile cvFile = new MockMultipartFile(
                "cv_file",
                "cv.txt",
                "text/plain",
                cvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/cv/generate-upload")
                        .file(cvFile)
                        .param("job_description", jobDescription))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cv_generated").exists())
                .andExpect(jsonPath("$.cv_generated.header.name").value("John Doe"))
                .andExpect(jsonPath("$.cv_generated.meta.job_title").value("Senior Backend Engineer"));
    }

    @Test
    void testGenerateCvFromUpload_withJsonFile() throws Exception {
        String cvJson = """
                {
                    "name": "Jane Smith",
                    "title": "Full Stack Developer",
                    "email": "jane@example.com",
                    "summary": ["Experienced full stack developer"],
                    "skills": [{"name": "JavaScript"}],
                    "experiences": [
                        {
                            "company": "Tech Inc",
                            "title": "Developer",
                            "start": "2020-01",
                            "end": "present"
                        }
                    ]
                }
                """;
        String jobDescription = "Full Stack Developer needed for modern web applications";

        MockMultipartFile cvFile = new MockMultipartFile(
                "cv_file",
                "cv.json",
                "application/json",
                cvJson.getBytes()
        );

        mockMvc.perform(multipart("/api/cv/generate-upload")
                        .file(cvFile)
                        .param("job_description", jobDescription))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cv_generated").exists())
                .andExpect(jsonPath("$.cv_generated.header.name").value("Jane Smith"))
                .andExpect(jsonPath("$.cv_generated.header.title").value("Full Stack Developer"));
    }

    @Test
    void testGenerateCvFromUpload_withOptions() throws Exception {
        String cvContent = "Bob Johnson\nData Scientist";
        String jobDescription = "Data Scientist position";
        String options = """
                {
                    "language": "en",
                    "max_experiences": 3
                }
                """;

        MockMultipartFile cvFile = new MockMultipartFile(
                "cv_file",
                "cv.txt",
                "text/plain",
                cvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/cv/generate-upload")
                        .file(cvFile)
                        .param("job_description", jobDescription)
                        .param("options", options))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cv_generated").exists())
                .andExpect(jsonPath("$.cv_generated.header.name").value("Bob Johnson"));
    }

    @Test
    void testGenerateCvFromUpload_missingCvFile() throws Exception {
        mockMvc.perform(multipart("/api/cv/generate-upload")
                        .param("job_description", "Some job"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateCvFromUpload_missingJobDescription() throws Exception {
        MockMultipartFile cvFile = new MockMultipartFile(
                "cv_file",
                "cv.txt",
                "text/plain",
                "Test CV".getBytes()
        );

        mockMvc.perform(multipart("/api/cv/generate-upload")
                        .file(cvFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateCvFromUpload_emptyCvFile() throws Exception {
        MockMultipartFile cvFile = new MockMultipartFile(
                "cv_file",
                "cv.txt",
                "text/plain",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/cv/generate-upload")
                        .file(cvFile)
                        .param("job_description", "Some job"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGenerateCvFromUpload_emptyJobDescription() throws Exception {
        MockMultipartFile cvFile = new MockMultipartFile(
                "cv_file",
                "cv.txt",
                "text/plain",
                "Test CV".getBytes()
        );

        mockMvc.perform(multipart("/api/cv/generate-upload")
                        .file(cvFile)
                        .param("job_description", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
