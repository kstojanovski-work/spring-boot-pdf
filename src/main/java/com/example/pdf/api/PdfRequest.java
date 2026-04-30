package com.example.pdf.api;

import jakarta.validation.constraints.NotBlank;

public record PdfRequest(
        @NotBlank(message = "html must not be blank")
        String html,
        String css,
        String fileName
) {
}
