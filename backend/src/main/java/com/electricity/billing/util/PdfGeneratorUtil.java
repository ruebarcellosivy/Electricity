package com.electricity.billing.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Renders simple single-page PDF documents (invoices, receipts, bill/complaint exports) using
 * Apache PDFBox. Kept intentionally minimal - a title, followed by label/value rows.
 */
public final class PdfGeneratorUtil {

    private static final PDFont TITLE_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont BODY_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private PdfGeneratorUtil() {
    }

    public static byte[] generateDocument(String title, List<String[]> rows) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50;
                float y = page.getMediaBox().getHeight() - margin;

                content.beginText();
                content.setFont(TITLE_FONT, 16);
                content.newLineAtOffset(margin, y);
                content.showText(title);
                content.endText();
                y -= 30;

                content.setLineWidth(1f);
                content.moveTo(margin, y);
                content.lineTo(page.getMediaBox().getWidth() - margin, y);
                content.stroke();
                y -= 25;

                for (String[] row : rows) {
                    content.beginText();
                    content.setFont(BODY_FONT, 11);
                    content.newLineAtOffset(margin, y);
                    content.showText(row[0] + ":");
                    content.endText();

                    content.beginText();
                    content.setFont(BODY_FONT, 11);
                    content.newLineAtOffset(margin + 180, y);
                    content.showText(row.length > 1 && row[1] != null ? row[1] : "");
                    content.endText();

                    y -= 22;
                    if (y < margin) {
                        break;
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate PDF document", e);
        }
    }
}
