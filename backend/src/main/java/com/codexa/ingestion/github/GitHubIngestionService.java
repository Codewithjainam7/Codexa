package com.codexa.ingestion.github;

import com.codexa.common.error.ApiException;
import com.codexa.config.CodexaProperties;
import com.codexa.ingestion.zip.ExtractionResult;
import com.codexa.ingestion.zip.SecureZipExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubIngestionService {

    private static final Logger log = LoggerFactory.getLogger(GitHubIngestionService.class);
    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("^https:\\/\\/github\\.com\\/([a-zA-Z0-9_.-]+)\\/([a-zA-Z0-9_.-]+?)(?:\\.git)?\\/?$");

    private final CodexaProperties properties;
    private final SecureZipExtractor zipExtractor;

    public GitHubIngestionService(CodexaProperties properties, SecureZipExtractor zipExtractor) {
        this.properties = properties;
        this.zipExtractor = zipExtractor;
    }

    public record RepoCoordinates(String owner, String repo) {}

    public boolean isValidGitHubUrl(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) {
            return false;
        }
        return GITHUB_URL_PATTERN.matcher(repoUrl.trim()).matches();
    }

    public void validateUrl(String repoUrl) {
        parseCoordinates(repoUrl);
    }

    public RepoCoordinates parseCoordinates(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_GITHUB_URL", "GitHub repository URL cannot be empty.");
        }

        Matcher matcher = GITHUB_URL_PATTERN.matcher(repoUrl.trim());
        if (!matcher.matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_GITHUB_URL",
                    "Invalid GitHub URL format. Must be an HTTPS link like https://github.com/owner/repository");
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        return new RepoCoordinates(owner, repo);
    }

    public ExtractionResult downloadAndExtract(String repoUrl, Path stagingDir) {
        RepoCoordinates coords = parseCoordinates(repoUrl);
        String archiveUrl = "https://api.github.com/repos/" + coords.owner() + "/" + coords.repo() + "/zipball";

        log.info("Fetching public GitHub repository archive from: {}", archiveUrl);

        try {
            HttpURLConnection connection = openSecureConnection(archiveUrl, 0);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new ApiException(HttpStatus.NOT_FOUND, "REPO_NOT_FOUND",
                        "GitHub repository not found or is private: " + repoUrl);
            }

            if (responseCode == 403 || responseCode == 429) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "GITHUB_RATE_LIMITED",
                        "GitHub API rate limit exceeded or access forbidden. Please try again later.");
            }

            if (responseCode < 200 || responseCode >= 300) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "GITHUB_DOWNLOAD_ERROR",
                        "GitHub returned HTTP status " + responseCode + " when requesting repository archive.");
            }

            try (InputStream in = connection.getInputStream()) {
                return zipExtractor.extract(in, stagingDir);
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to download or extract GitHub archive from {}: {}", repoUrl, e.getMessage(), e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "GITHUB_DOWNLOAD_FAILED",
                    "Could not fetch repository archive from GitHub: " + e.getMessage(), e);
        }
    }

    private HttpURLConnection openSecureConnection(String targetUrl, int redirectCount) throws Exception {
        if (redirectCount > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TOO_MANY_REDIRECTS", "Too many redirects encountered while fetching GitHub archive.");
        }

        URI uri = URI.create(targetUrl);
        String host = uri.getHost();

        // SSRF defense: only allow GitHub domains
        if (host == null || (!host.equalsIgnoreCase("github.com") &&
                !host.equalsIgnoreCase("api.github.com") &&
                !host.equalsIgnoreCase("codeload.github.com") &&
                !host.endsWith(".githubusercontent.com"))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SSRF_BLOCKED", "Redirect to unauthorized host blocked: " + host);
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INSECURE_PROTOCOL", "Insecure non-HTTPS connection blocked.");
        }

        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("User-Agent", "Codexa-Security-Scanner/1.0");
        conn.setRequestProperty("Accept", "application/vnd.github+json");

        String githubToken = System.getenv("GITHUB_TOKEN");
        if (githubToken != null && !githubToken.isBlank()) {
            conn.setRequestProperty("Authorization", "Bearer " + githubToken.trim());
        }

        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == 308) {
            String newLocation = conn.getHeaderField("Location");
            if (newLocation == null || newLocation.isBlank()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "INVALID_REDIRECT", "GitHub returned redirect without Location header.");
            }
            conn.disconnect();
            return openSecureConnection(newLocation, redirectCount + 1);
        }

        return conn;
    }
}
