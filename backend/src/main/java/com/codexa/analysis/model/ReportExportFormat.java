package com.codexa.analysis.model;

/**
 * Enumerates all export formats supported by Codexa Report Export Service.
 */
public enum ReportExportFormat {
    PDF("pdf", "application/pdf"),
    HTML("html", "text/html"),
    MARKDOWN("md", "text/markdown"),
    JSON("json", "application/json"),
    CSV("csv", "text/csv"),
    SARIF("sarif", "application/sarif+json");

    private final String extension;
    private final String mediaType;

    ReportExportFormat(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static ReportExportFormat fromString(String val) {
        if (val == null) return PDF;
        for (ReportExportFormat fmt : values()) {
            if (fmt.name().equalsIgnoreCase(val) || fmt.extension.equalsIgnoreCase(val)) {
                return fmt;
            }
        }
        return PDF;
    }
}
