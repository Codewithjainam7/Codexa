package com.codexa.ingestion.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Set;

@Service
public class FileFilterService {

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            "target", "build", "out", ".git", ".svn", ".hg",
            "node_modules", ".idea", ".vscode", ".gradle",
            "dist", "bin", "coverage", "__pycache__", ".staging",
            ".next", ".turbo", ".nuxt", ".cache"
    );

    private static final Set<String> ALLOWED_SOURCE_EXTENSIONS = Set.of(
            // JVM
            ".java", ".kt", ".scala", ".groovy",
            // TypeScript & JavaScript
            ".ts", ".tsx", ".js", ".jsx", ".mjs", ".cjs", ".vue", ".svelte",
            // Python & Backend
            ".py", ".go", ".rs", ".php", ".rb", ".cs", ".cpp", ".c", ".h", ".hpp",
            // Web, Markup & Configs
            ".html", ".css", ".xml", ".properties", ".yml", ".yaml",
            ".json", ".gradle", ".env", ".sql", ".md", ".toml", ".ini"
    );

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".class", ".jar", ".war", ".ear", ".exe", ".dll", ".so",
            ".dylib", ".zip", ".tar", ".gz", ".7z", ".png", ".jpg",
            ".jpeg", ".gif", ".svg", ".ico", ".pdf", ".mp4", ".mp3",
            ".woff", ".woff2", ".ttf", ".eot", ".lock", ".map"
    );

    public boolean isIgnoredDirectory(Path relativePath) {
        for (Path segment : relativePath) {
            if (IGNORED_DIRECTORIES.contains(segment.toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSupportedAnalysisFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }

        String lower = filename.toLowerCase();

        // Exact filenames of interest
        if (lower.equals("dockerfile") || lower.equals("pom.xml") || lower.equals(".env") || 
            lower.equals(".env.local") || lower.equals(".env.example") || lower.equals("package.json") ||
            lower.equals("requirements.txt") || lower.equals("gemfile") || lower.equals("cargo.toml")) {
            return true;
        }

        for (String ignoredExt : IGNORED_EXTENSIONS) {
            if (lower.endsWith(ignoredExt)) {
                return false;
            }
        }

        for (String allowedExt : ALLOWED_SOURCE_EXTENSIONS) {
            if (lower.endsWith(allowedExt)) {
                return true;
            }
        }

        return false;
    }

    public boolean isJavaSourceFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".java");
    }

    public boolean isTypeScriptOrJavaScript(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".ts") || lower.endsWith(".tsx") || lower.endsWith(".js") || lower.endsWith(".jsx");
    }

    public boolean isPythonSourceFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".py");
    }
}
