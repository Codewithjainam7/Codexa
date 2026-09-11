package com.codexa.analysis.service;

/**
 * Encapsulates CSS styling and responsive layout rules for standalone HTML audit reports.
 */
public final class HtmlReportThemeBuilder {

    private HtmlReportThemeBuilder() {}

    public static String getEmbeddedCss() {
        return """
            :root {
                --bg-primary: #0C0D10;
                --bg-secondary: #14161C;
                --text-primary: #F8FAFC;
                --text-muted: #94A3B8;
                --border-color: #2D3748;
                --accent-blue: #3B82F6;
                --accent-rose: #F43F5E;
                --accent-emerald: #10B981;
                --accent-amber: #F59E0B;
            }
            body { font-family: 'Inter', -apple-system, sans-serif; background: var(--bg-primary); color: var(--text-primary); margin: 0; padding: 24px; }
            .card { background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: 12px; padding: 20px; margin-bottom: 20px; }
            .badge { display: inline-block; padding: 4px 10px; border-radius: 9999px; font-weight: 700; font-size: 11px; text-transform: uppercase; }
            .badge-critical { background: rgba(244, 63, 94, 0.15); color: var(--accent-rose); border: 1px solid var(--accent-rose); }
            .badge-high { background: rgba(245, 158, 11, 0.15); color: var(--accent-amber); border: 1px solid var(--accent-amber); }
            pre { background: #07080A; padding: 12px; border-radius: 8px; font-family: monospace; font-size: 12px; overflow-x: auto; }
        """;
    }
}
