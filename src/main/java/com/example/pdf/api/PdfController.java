package com.example.pdf.api;

import com.example.pdf.render.PdfRenderService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfRenderService pdfRenderService;

    public PdfController(PdfRenderService pdfRenderService) {
        this.pdfRenderService = pdfRenderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> renderPdf(@Valid @RequestBody PdfRequest request) {
        // Convert the submitted HTML and optional CSS into raw PDF bytes.
        byte[] pdf = pdfRenderService.render(request.html(), request.css());
        String fileName = sanitizeFileName(request.fileName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                // "inline" lets browsers preview the PDF instead of forcing a download.
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(fileName)
                        .build()
                        .toString())
                .body(pdf);
    }

    private String sanitizeFileName(String fileName) {
        // Use a stable fallback when the client does not send a usable file name.
        if (fileName == null || fileName.isBlank()) {
            return "document.pdf";
        }

        // Keep the header value simple and safe for Content-Disposition.
        String sanitized = fileName.trim()
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("^[._-]+", "");

        if (sanitized.isBlank()) {
            return "document.pdf";
        }

        return sanitized.toLowerCase(Locale.ROOT).endsWith(".pdf") ? sanitized : sanitized + ".pdf";
    }
}
