package com.example.pdf.render;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfRenderService {

    private static final String DEFAULT_CSS = """
            @page {
              size: A4;
              margin: 20mm;
            }

            body {
              font-family: Arial, Helvetica, sans-serif;
              font-size: 12pt;
              line-height: 1.45;
              color: #1f2933;
            }

            h1, h2, h3 {
              color: #102a43;
              margin: 0 0 12px;
            }

            table {
              width: 100%;
              border-collapse: collapse;
            }

            th, td {
              border: 1px solid #d9e2ec;
              padding: 6px 8px;
            }
            """;

    public byte[] render(String html, String css) {
        String xhtml = toXhtml(html, css);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new PdfRenderException("PDF could not be rendered from HTML", exception);
        }
    }

    private String toXhtml(String html, String css) {
        Document document = Jsoup.parse(html);
        document.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset("UTF-8");

        Element head = document.head();
        head.prependElement("meta").attr("charset", "UTF-8");
        head.appendElement("style").appendText(DEFAULT_CSS);

        if (css != null && !css.isBlank()) {
            head.appendElement("style").appendText(css);
        }

        return document.html();
    }
}
