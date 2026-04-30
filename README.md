# Spring Boot PDF Renderer

This project provides a REST endpoint that accepts HTML and optional CSS and
renders a PDF from it. It also includes a small frontend for selecting,
editing, and rendering templates directly as PDF previews.

## Start

```bash
mvn spring-boot:run
```

The frontend is then available at:

```text
http://localhost:8080
```

## Frontend

The frontend lives in `src/main/resources/static/index.html`, with styling in
`src/main/resources/static/app.css`.

It currently includes three templates:

- `Demo`: simple HTML/CSS test
- `Enterprise`: professional invoice style
- `Prospekt`: multi-page brochure/marketing layout

When a template is selected, the file name, HTML, and CSS are loaded into the
input fields. The `PDF erzeugen` button sends the data to `/api/pdf` and shows
the response as a PDF preview.

The template list lives in `src/main/resources/static/templates/index.json`.
The actual template content is stored next to it as separate HTML and CSS files,
for example:

```text
src/main/resources/static/templates/demo.html
src/main/resources/static/templates/demo.css
```

## Render A PDF

```bash
curl -X POST http://localhost:8080/api/pdf \
  -H 'Content-Type: application/json' \
  --output document.pdf \
  -d '{
    "html": "<h1>Invoice</h1><p>Hello PDF</p>",
    "css": "h1 { color: #006d77; } body { font-family: Arial; }",
    "fileName": "invoice.pdf"
  }'
```

The response uses `Content-Type: application/pdf` and returns the PDF file as a
byte stream.

## Request

```json
{
  "html": "<h1>Title</h1><p>Content</p>",
  "css": "h1 { color: steelblue; }",
  "fileName": "document.pdf"
}
```

`html` is required. `css` and `fileName` are optional.

## PDF Page CSS

PDF page margins are controlled with `@page`:

```css
@page {
  size: A4;
  margin: 20mm;
}
```

For a borderless first page and regular margins from page 2 onward:

```css
@page {
  size: A4;
  margin: 24mm 18mm 18mm;
}

@page :first {
  margin: 0;
}
```

HTML-to-PDF rendering is not always identical to browser rendering. For stable
layouts, fixed units such as `mm` are often more reliable than purely percentage
based widths.

## Architecture

- `PdfController`: HTTP endpoint, response headers, and file name handling
- `PdfRequest`: request data and validation
- `PdfRenderService`: normalizes HTML/CSS to XHTML and renders the PDF
- `RestExceptionHandler`: clean error responses as `ProblemDetail`
- `index.html`: manual test frontend with template selection
- `app.css`: frontend styling
- `templates/*.html` and `templates/*.css`: editable PDF templates

Responsibilities are separated into web/API, rendering, and error handling
classes.

## Tests

```bash
mvn test
```

The tests cover:

- PDF rendering from HTML/CSS
- validation for blank HTML
- default file name behavior
- file name sanitizing
- frontend delivery
- delivery of frontend CSS and template files
