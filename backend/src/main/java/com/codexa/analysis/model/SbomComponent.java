package com.codexa.analysis.model;

/**
 * Represents a software dependency component in a Software Bill of Materials (SBOM).
 */
public record SbomComponent(
        String name,
        String version,
        String ecosystem,     // maven, npm, pypi, golang
        String group,         // e.g. org.apache.logging.log4j
        String license,       // e.g. Apache-2.0, MIT
        String purl,          // Package URL, e.g. pkg:maven/org.apache.logging.log4j/log4j-core@2.14.1
        boolean isDirect,
        String filePath,      // e.g. pom.xml or package.json
        int lineNumber
) {
    public static SbomComponent of(String name, String version, String ecosystem, String filePath, int lineNumber) {
        String purl = "pkg:" + ecosystem.toLowerCase() + "/" + name + "@" + version;
        return new SbomComponent(name, version, ecosystem, "", "UNKNOWN", purl, true, filePath, lineNumber);
    }

    public static SbomComponent ofMaven(String group, String artifact, String version, String filePath, int lineNumber) {
        String purl = "pkg:maven/" + group + "/" + artifact + "@" + version;
        return new SbomComponent(artifact, version, "maven", group, "UNKNOWN", purl, true, filePath, lineNumber);
    }
}
