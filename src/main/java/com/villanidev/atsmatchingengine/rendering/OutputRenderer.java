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
        CvTemplate template = registry.getTemplateForProfile(templateName);
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
        List<StyledLine> lines = parseMarkdownLines(markdown);
        if (lines.isEmpty()) {
            return null;
        }
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, y);

            for (StyledLine line : lines) {
                PdfStyle style = PdfStyle.from(line.style);
                contentStream.setFont(style.font, style.fontSize);
                contentStream.setLeading(style.leading);

                List<String> wrapped = wrapLines(line.text, style.maxChars);
                for (String wrappedLine : wrapped) {
                    if (y <= margin + style.leading) {
                        contentStream.endText();
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        y = page.getMediaBox().getHeight() - margin;
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, y);
                    }
                    contentStream.showText(wrappedLine);
                    contentStream.newLine();
                    y -= style.leading;
                }

                if (line.style == LineStyle.SECTION_SEPARATOR) {
                    y -= 6;
                    contentStream.newLine();
                }
            }

            contentStream.endText();
            contentStream.close();

            document.save(outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    private String generateDocxBase64(String markdown) {
        List<StyledLine> lines = parseMarkdownLines(markdown);
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (StyledLine line : lines) {
                if (line.style == LineStyle.SECTION_SEPARATOR) {
                    document.createParagraph();
                    continue;
                }

                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                DocxStyle style = DocxStyle.from(line.style);
                run.setBold(style.bold);
                run.setFontSize(style.fontSize);

                if (line.style == LineStyle.BULLET) {
                    paragraph.setIndentationLeft(360);
                    run.setText("• " + line.text);
                } else {
                    run.setText(line.text);
                }
            }
            document.write(outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    private List<StyledLine> parseMarkdownLines(String markdown) {
        List<StyledLine> lines = new java.util.ArrayList<>();
        if (markdown == null || markdown.isBlank()) {
            return lines;
        }

        for (String rawLine : markdown.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                lines.add(new StyledLine("", LineStyle.SECTION_SEPARATOR));
                continue;
            }

            if (line.startsWith("### ")) {
                lines.add(new StyledLine(cleanMarkdown(line.substring(4)), LineStyle.HEADING_3));
                continue;
            }
            if (line.startsWith("## ")) {
                lines.add(new StyledLine(cleanMarkdown(line.substring(3)), LineStyle.HEADING_2));
                continue;
            }
            if (line.startsWith("# ")) {
                lines.add(new StyledLine(cleanMarkdown(line.substring(2)), LineStyle.HEADING_1));
                continue;
            }
            if (line.startsWith("- ")) {
                lines.add(new StyledLine(cleanMarkdown(line.substring(2)), LineStyle.BULLET));
                continue;
            }

            lines.add(new StyledLine(cleanMarkdown(line), LineStyle.BODY));
        }

        return lines;
    }

    private String cleanMarkdown(String text) {
        return text
                .replaceAll("\\*\\*", "")
                .replaceAll("\\*", "")
                .replaceAll("`", "")
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
        String sanitized = sanitizePdfText(text);
        if (sanitized.isBlank()) {
            return List.of("");
        }
        String[] words = sanitized.split("\\s+");
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

    private enum LineStyle {
        HEADING_1,
        HEADING_2,
        HEADING_3,
        BODY,
        BULLET,
        SECTION_SEPARATOR
    }

    private static class StyledLine {
        private final String text;
        private final LineStyle style;

        private StyledLine(String text, LineStyle style) {
            this.text = text;
            this.style = style;
        }
    }

    private static class PdfStyle {
        private final PDType1Font font;
        private final float fontSize;
        private final float leading;
        private final int maxChars;

        private PdfStyle(PDType1Font font, float fontSize, float leading, int maxChars) {
            this.font = font;
            this.fontSize = fontSize;
            this.leading = leading;
            this.maxChars = maxChars;
        }

        private static PdfStyle from(LineStyle style) {
            return switch (style) {
                case HEADING_1 -> new PdfStyle(PDType1Font.HELVETICA_BOLD, 18f, 22f, 60);
                case HEADING_2 -> new PdfStyle(PDType1Font.HELVETICA_BOLD, 14f, 18f, 75);
                case HEADING_3 -> new PdfStyle(PDType1Font.HELVETICA_BOLD, 12f, 16f, 85);
                case BULLET -> new PdfStyle(PDType1Font.HELVETICA, 11f, 15f, 90);
                case BODY -> new PdfStyle(PDType1Font.HELVETICA, 11f, 15f, 95);
                case SECTION_SEPARATOR -> new PdfStyle(PDType1Font.HELVETICA, 11f, 10f, 95);
            };
        }
    }

    private static class DocxStyle {
        private final boolean bold;
        private final int fontSize;

        private DocxStyle(boolean bold, int fontSize) {
            this.bold = bold;
            this.fontSize = fontSize;
        }

        private static DocxStyle from(LineStyle style) {
            return switch (style) {
                case HEADING_1 -> new DocxStyle(true, 20);
                case HEADING_2 -> new DocxStyle(true, 16);
                case HEADING_3 -> new DocxStyle(true, 13);
                case BULLET -> new DocxStyle(false, 11);
                case BODY -> new DocxStyle(false, 11);
                case SECTION_SEPARATOR -> new DocxStyle(false, 11);
            };
        }
    }
}
