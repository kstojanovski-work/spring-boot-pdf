package com.example.pdf.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersPdfFromHtmlAndCss() throws Exception {
        String request = """
                {
                  "html": "<h1>Rechnung</h1><p>Hallo PDF</p>",
                  "css": "h1 { color: #006d77; }",
                  "fileName": "rechnung.pdf"
                }
                """;

        mockMvc.perform(post("/api/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", startsWith("inline; filename=\"rechnung.pdf\"")))
                .andExpect(result -> {
                    byte[] pdf = result.getResponse().getContentAsByteArray();
                    String header = new String(pdf, 0, Math.min(pdf.length, 4));
                    org.assertj.core.api.Assertions.assertThat(header).isEqualTo("%PDF");
                });
    }

    @Test
    void rejectsBlankHtml() throws Exception {
        mockMvc.perform(post("/api/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"html\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usesDefaultPdfFileNameWhenNoneIsProvided() throws Exception {
        String request = """
                {
                  "html": "<h1>Dokument</h1>"
                }
                """;

        mockMvc.perform(post("/api/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", startsWith("inline; filename=\"document.pdf\"")));
    }

    @Test
    void sanitizesPdfFileNameForContentDisposition() throws Exception {
        String request = """
                {
                  "html": "<h1>Dokument</h1>",
                  "fileName": "../kunden rechnung"
                }
                """;

        mockMvc.perform(post("/api/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", startsWith("inline; filename=\"kunden_rechnung.pdf\"")));
    }

    @Test
    void servesFrontend() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("grid-template-columns")));
    }

    @Test
    void servesTemplateFiles() throws Exception {
        mockMvc.perform(get("/templates/index.json"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\": \"demo\"")));

        mockMvc.perform(get("/templates/demo.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1>Rechnung</h1>")));

        mockMvc.perform(get("/templates/demo.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("color: #006d77;")));
    }
}
