package com.villanidev.atsmatchingengine.rendering;

import com.villanidev.atsmatchingengine.domain.CvGenerated;
import com.villanidev.atsmatchingengine.domain.Options;
import com.villanidev.atsmatchingengine.templates.CvTemplate;
import com.villanidev.atsmatchingengine.templates.TemplateRegistry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class OutputRenderer {

    public CvGenerated.Output buildOutput(CvGenerated cvGenerated, Options options) {
        CvGenerated.Output output = new CvGenerated.Output();

        TemplateRegistry registry = new TemplateRegistry();
        String templateName = options != null ? options.getProfile() : null;
        CvTemplate template = registry.getTemplate(templateName);
        String markdown = template.renderMarkdown(cvGenerated);
        output.setMarkdown(markdown);

        if (shouldIncludeFormat(options, "pdf")) {
            output.setPdfBase64(generatePdfBase64(markdown));
        }

        if (shouldIncludeFormat(options, "docx")) {
            output.setDocxBase64(generateDocxBase64(markdown));
        }

        return output;
    }

    private boolean shouldIncludeFormat(Options options, String format) {
        if (options == null || options.getOutputFormats() == null || options.getOutputFormats().isEmpty()) {
            return "pdf".equalsIgnoreCase(format) || "docx".equalsIgnoreCase(format);
        }
        return options.getOutputFormats().stream()
                .map(f -> f.toLowerCase(Locale.ROOT))
                .anyMatch(f -> f.equals(format));
    }

    private String generatePdfBase64(String markdown) {
        String text = sanitizePdfText(markdownToPlainText(markdown));
        if (text.isBlank()) {
            return null;
        }
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.setLeading(14.5f);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);

                for (String line : wrapLines(text, 95)) {
                    contentStream.showText(line);
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            document.save(outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    private String generateDocxBase64(String markdown) {
        String text = markdownToPlainText(markdown);
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (String line : text.split("\\r?\\n")) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            document.write(outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    private String markdownToPlainText(String markdown) {
        return markdown
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .replaceAll("\\*\\*", "")
                .replaceAll("\\*", "")
                .replaceAll("`", "")
                .replaceAll("\\r", "")
                .trim();
    }

    private String sanitizePdfText(String text) {
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[^\\x20-\\x7E\\n]", " ");
        normalized = normalized.replaceAll("\\s{2,}", " ");
        return normalized.trim();
    }

    private List<String> wrapLines(String text, int maxLength) {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (String word : words) {
            if (line.length() + word.length() + 1 > maxLength) {
                lines.add(line.toString().trim());
                line.setLength(0);
            }
            line.append(word).append(' ');
        }
        if (!line.isEmpty()) {
            lines.add(line.toString().trim());
        }
        return lines;
    }
}
