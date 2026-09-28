package com.codexa.analysis.service;

import com.codexa.analysis.model.CveAdvisory;
import com.codexa.analysis.model.SbomComponent;
import com.codexa.analysis.model.SbomReport;
import com.codexa.analysis.model.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise Software Bill of Materials (SBOM) and Dependency CVE Scanning Engine.
 * Parses Maven, Gradle, npm, PyPI, and Go manifests, cross-referencing against CVE advisories
 * and generating CycloneDX v1.5 / SPDX v2.3 SBOM standards.
 */
@Service
public class SbomDependencyService {

    private static final Logger log = LoggerFactory.getLogger(SbomDependencyService.class);

    // Built-in CVE Knowledge Base covering critical supply chain vulnerabilities
    private static final List<CveAdvisory> ADVISORIES = List.of(
            // JVM / Maven
            new CveAdvisory("CVE-2021-44228", "log4j-core", "maven", "^(2\\.(0|[1-9]|1[0-4])(\\..*)?|2\\.15\\.0|2\\.16\\.0)$", "2.17.1", 10.0, Severity.CRITICAL, "Log4Shell JNDI Remote Code Execution", "CWE-502", "https://nvd.nist.gov/vuln/detail/CVE-2021-44228"),
            new CveAdvisory("CVE-2022-22965", "spring-beans", "maven", "^(5\\.[0-2]\\..*|5\\.3\\.(0|[1-9]|1[0-7]))$", "5.3.18", 9.8, Severity.CRITICAL, "Spring4Shell ClassLoader Access RCE", "CWE-94", "https://nvd.nist.gov/vuln/detail/CVE-2022-22965"),
            new CveAdvisory("CVE-2022-1471", "snakeyaml", "maven", "^(1\\..*|2\\.0-.*)$", "2.0", 9.8, Severity.CRITICAL, "SnakeYAML Constructor Deserialization RCE", "CWE-502", "https://nvd.nist.gov/vuln/detail/CVE-2022-1471"),
            new CveAdvisory("CVE-2020-36518", "jackson-databind", "maven", "^(2\\.(0|[1-9]|1[0-2])\\..*|2\\.13\\.[0-2])$", "2.13.2.1", 7.5, Severity.HIGH, "Jackson-databind Java StackOverflow DoS / Deserialization", "CWE-787", "https://nvd.nist.gov/vuln/detail/CVE-2020-36518"),
            new CveAdvisory("CVE-2022-42889", "commons-text", "maven", "^(1\\.[5-9])$", "1.10.0", 9.8, Severity.CRITICAL, "Text4Shell Variable Interpolation RCE", "CWE-94", "https://nvd.nist.gov/vuln/detail/CVE-2022-42889"),

            // JavaScript / npm
            new CveAdvisory("CVE-2019-10744", "lodash", "npm", "^([0-3]\\..*|4\\.(0|[1-9]|1[0-6])\\..*|4\\.17\\.(0|[1-9]|1[0-8]))$", "4.17.19", 9.1, Severity.CRITICAL, "Lodash Prototype Pollution via defaultsDeep", "CWE-1321", "https://nvd.nist.gov/vuln/detail/CVE-2019-10744"),
            new CveAdvisory("CVE-2020-8203", "lodash", "npm", "^(4\\.17\\.(19|20))$", "4.17.21", 7.4, Severity.HIGH, "Lodash Prototype Pollution via zipObjectDeep", "CWE-1321", "https://nvd.nist.gov/vuln/detail/CVE-2020-8203"),
            new CveAdvisory("CVE-2020-7598", "minimist", "npm", "^(0\\..*|1\\.(0|1)\\..*|1\\.2\\.[0-5])$", "1.2.6", 7.5, Severity.HIGH, "Minimist Prototype Pollution in argument parsing", "CWE-1321", "https://nvd.nist.gov/vuln/detail/CVE-2020-7598"),
            new CveAdvisory("CVE-2021-37701", "tar", "npm", "^([0-5]\\..*|6\\.(0\\..*|1\\.[0-8]))$", "6.1.9", 7.5, Severity.HIGH, "Node-tar Arbitrary File Overwrite via Path Traversal", "CWE-22", "https://nvd.nist.gov/vuln/detail/CVE-2021-37701"),

            // Python / PyPI
            new CveAdvisory("CVE-2020-14343", "pyyaml", "npm", "^([0-4]\\..*|5\\.[0-3](\\..*)?)$", "5.4", 9.8, Severity.CRITICAL, "PyYAML Arbitrary Code Execution through full_load", "CWE-502", "https://nvd.nist.gov/vuln/detail/CVE-2020-14343"),
            new CveAdvisory("CVE-2023-45803", "urllib3", "pypi", "^([0-1]\\..*|2\\.0\\.[0-6])$", "2.0.7", 7.5, Severity.HIGH, "Urllib3 Request Body Stream Leak on 303 Redirect", "CWE-200", "https://nvd.nist.gov/vuln/detail/CVE-2023-45803"),
            new CveAdvisory("CVE-2022-28346", "django", "pypi", "^(2\\..*|3\\.[0-1]\\..*|3\\.2\\.(0|[1-9]|1[0-2])|4\\.0\\.[0-3])$", "3.2.13", 8.8, Severity.HIGH, "Django SQL Injection in QuerySet.annotate/aggregate", "CWE-89", "https://nvd.nist.gov/vuln/detail/CVE-2022-28346")
    );

    /**
     * Inspects all manifest files in the repository directory and generates an SBOM report.
     */
    public SbomReport generateSbom(Path rootDir, UUID jobId, String projectName) {
        List<SbomComponent> components = new ArrayList<>();
        List<CveAdvisory> detectedVulnerabilities = new ArrayList<>();

        if (rootDir == null || !Files.exists(rootDir)) {
            return new SbomReport(jobId, projectName, "1.5", Instant.now(), 0, 0, List.of(), List.of());
        }

        try {
            Files.walk(rootDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String name = file.getFileName().toString().toLowerCase();
                        try {
                            if (name.equals("pom.xml")) {
                                components.addAll(parsePomXml(file, rootDir));
                            } else if (name.endsWith(".gradle") || name.endsWith(".gradle.kts")) {
                                components.addAll(parseGradle(file, rootDir));
                            } else if (name.equals("package.json")) {
                                components.addAll(parsePackageJson(file, rootDir));
                            } else if (name.equals("requirements.txt")) {
                                components.addAll(parseRequirementsTxt(file, rootDir));
                            } else if (name.equals("go.mod")) {
                                components.addAll(parseGoMod(file, rootDir));
                            }
                        } catch (Exception ex) {
                            log.debug("Error parsing manifest file '{}': {}", file, ex.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to walk rootDir for SBOM extraction: {}", e.getMessage());
        }

        // Cross-reference components with known CVE advisories
        Set<String> seenCves = new HashSet<>();
        for (SbomComponent comp : components) {
            for (CveAdvisory adv : ADVISORIES) {
                if (comp.name().equalsIgnoreCase(adv.componentName())) {
                    if (isVersionVulnerable(comp.version(), adv.vulnerableVersionMatcher())) {
                        String key = adv.cveId() + ":" + comp.name();
                        if (!seenCves.contains(key)) {
                            seenCves.add(key);
                            detectedVulnerabilities.add(adv);
                        }
                    }
                }
            }
        }

        return new SbomReport(
                jobId,
                projectName,
                "1.5",
                Instant.now(),
                components.size(),
                detectedVulnerabilities.size(),
                components,
                detectedVulnerabilities
        );
    }

    public List<CveAdvisory> getAdvisories() {
        return ADVISORIES;
    }

    public boolean isVersionVulnerable(String version, String regexPattern) {
        if (version == null || regexPattern == null) return false;
        String cleanVersion = version.replaceAll("[^0-9a-zA-Z.-]", "").trim();
        try {
            return Pattern.compile(regexPattern).matcher(cleanVersion).matches();
        } catch (Exception e) {
            return cleanVersion.equals(regexPattern);
        }
    }

    /**
     * Parses Maven pom.xml files.
     */
    public List<SbomComponent> parsePomXml(Path pomFile, Path rootDir) throws IOException {
        List<SbomComponent> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(pomFile, StandardCharsets.UTF_8);
        String relPath = rootDir.relativize(pomFile).toString().replace('\\', '/');

        boolean inDep = false;
        String currentGroup = "";
        String currentArtifact = "";
        String currentVersion = "";
        int startLine = 1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            int lineNum = i + 1;

            if (line.contains("<dependency>")) {
                inDep = true;
                currentGroup = "";
                currentArtifact = "";
                currentVersion = "";
                startLine = lineNum;
            } else if (line.contains("</dependency>")) {
                if (inDep && !currentArtifact.isEmpty()) {
                    list.add(SbomComponent.ofMaven(currentGroup, currentArtifact, currentVersion.isEmpty() ? "LATEST" : currentVersion, relPath, startLine));
                }
                inDep = false;
            } else if (inDep) {
                if (line.contains("<groupId>")) {
                    currentGroup = extractXmlValue(line, "groupId");
                } else if (line.contains("<artifactId>")) {
                    currentArtifact = extractXmlValue(line, "artifactId");
                } else if (line.contains("<version>")) {
                    currentVersion = extractXmlValue(line, "version");
                }
            }
        }
        return list;
    }

    /**
     * Parses Gradle build scripts.
     */
    public List<SbomComponent> parseGradle(Path gradleFile, Path rootDir) throws IOException {
        List<SbomComponent> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(gradleFile, StandardCharsets.UTF_8);
        String relPath = rootDir.relativize(gradleFile).toString().replace('\\', '/');

        Pattern p = Pattern.compile("['\"]([a-zA-Z0-9._-]+):([a-zA-Z0-9._-]+):([a-zA-Z0-9._-]+)['\"]");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher m = p.matcher(line);
            if (m.find()) {
                list.add(SbomComponent.ofMaven(m.group(1), m.group(2), m.group(3), relPath, i + 1));
            }
        }
        return list;
    }

    /**
     * Parses npm package.json manifests.
     */
    public List<SbomComponent> parsePackageJson(Path packageJsonFile, Path rootDir) throws IOException {
        List<SbomComponent> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(packageJsonFile, StandardCharsets.UTF_8);
        String relPath = rootDir.relativize(packageJsonFile).toString().replace('\\', '/');

        boolean inDeps = false;
        Pattern depPattern = Pattern.compile("\"([@a-zA-Z0-9_/-]+)\"\\s*:\\s*\"([^\"]+)\"");

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("\"dependencies\"") || line.contains("\"devDependencies\"")) {
                inDeps = true;
                continue;
            }
            if (inDeps && line.contains("}")) {
                inDeps = false;
                continue;
            }
            if (inDeps) {
                Matcher m = depPattern.matcher(line);
                if (m.find()) {
                    String name = m.group(1);
                    String version = m.group(2).replaceAll("[^0-9.]", "").trim();
                    list.add(SbomComponent.of(name, version.isEmpty() ? m.group(2) : version, "npm", relPath, i + 1));
                }
            }
        }
        return list;
    }

    /**
     * Parses Python requirements.txt files.
     */
    public List<SbomComponent> parseRequirementsTxt(Path reqFile, Path rootDir) throws IOException {
        List<SbomComponent> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(reqFile, StandardCharsets.UTF_8);
        String relPath = rootDir.relativize(reqFile).toString().replace('\\', '/');

        Pattern p = Pattern.compile("^([a-zA-Z0-9_.-]+)\\s*==\\s*([a-zA-Z0-9_.-]+)");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("#") || line.isEmpty()) continue;
            Matcher m = p.matcher(line);
            if (m.find()) {
                list.add(SbomComponent.of(m.group(1), m.group(2), "pypi", relPath, i + 1));
            }
        }
        return list;
    }

    /**
     * Parses Go go.mod manifests.
     */
    public List<SbomComponent> parseGoMod(Path goModFile, Path rootDir) throws IOException {
        List<SbomComponent> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(goModFile, StandardCharsets.UTF_8);
        String relPath = rootDir.relativize(goModFile).toString().replace('\\', '/');

        Pattern p = Pattern.compile("^\\s*([a-zA-Z0-9._/-]+)\\s+(v[0-9a-zA-Z.-]+)");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher m = p.matcher(line);
            if (m.find()) {
                list.add(SbomComponent.of(m.group(1), m.group(2), "golang", relPath, i + 1));
            }
        }
        return list;
    }

    /**
     * Serializes an SBOM report to OASIS / OWASP CycloneDX v1.5 standard JSON.
     */
    public String exportCycloneDxJson(SbomReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"bomFormat\": \"CycloneDX\",\n");
        sb.append("  \"specVersion\": \"1.5\",\n");
        sb.append("  \"serialNumber\": \"urn:uuid:").append(report.jobId() != null ? report.jobId() : UUID.randomUUID()).append("\",\n");
        sb.append("  \"version\": 1,\n");
        sb.append("  \"metadata\": {\n");
        sb.append("    \"timestamp\": \"").append(report.generatedAt()).append("\",\n");
        sb.append("    \"tools\": [{\"vendor\": \"Codexa\", \"name\": \"Codexa SBOM Scanner\", \"version\": \"1.3.0\"}],\n");
        sb.append("    \"component\": {\"name\": \"").append(escapeJson(report.projectName())).append("\", \"type\": \"application\"}\n");
        sb.append("  },\n");
        sb.append("  \"components\": [\n");

        for (int i = 0; i < report.components().size(); i++) {
            SbomComponent c = report.components().get(i);
            sb.append("    {\n");
            sb.append("      \"type\": \"library\",\n");
            if (!c.group().isEmpty()) {
                sb.append("      \"group\": \"").append(escapeJson(c.group())).append("\",\n");
            }
            sb.append("      \"name\": \"").append(escapeJson(c.name())).append("\",\n");
            sb.append("      \"version\": \"").append(escapeJson(c.version())).append("\",\n");
            sb.append("      \"purl\": \"").append(escapeJson(c.purl())).append("\"\n");
            sb.append("    }").append(i < report.components().size() - 1 ? "," : "").append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String extractXmlValue(String line, String tag) {
        int start = line.indexOf("<" + tag + ">");
        int end = line.indexOf("</" + tag + ">");
        if (start != -1 && end != -1) {
            return line.substring(start + tag.length() + 2, end).trim();
        }
        return "";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
