package com.codexa.analysis.service;

import com.codexa.analysis.model.SbomComponent;
import com.codexa.analysis.model.SbomReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SbomDependencyServiceTest {

    private SbomDependencyService sbomService;

    @BeforeEach
    void setUp() {
        sbomService = new SbomDependencyService();
    }

    @Test
    void shouldParseMavenPomXmlDependencies(@TempDir Path tempDir) throws IOException {
        String pomXml = """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <dependencies>
                    <dependency>
                        <groupId>org.apache.logging.log4j</groupId>
                        <artifactId>log4j-core</artifactId>
                        <version>2.14.1</version>
                    </dependency>
                    <dependency>
                        <groupId>org.yaml</groupId>
                        <artifactId>snakeyaml</artifactId>
                        <version>1.33</version>
                    </dependency>
                </dependencies>
            </project>
            """;
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomXml);

        List<SbomComponent> components = sbomService.parsePomXml(pomFile, tempDir);

        assertThat(components).hasSize(2);
        assertThat(components.get(0).name()).isEqualTo("log4j-core");
        assertThat(components.get(0).version()).isEqualTo("2.14.1");
        assertThat(components.get(0).ecosystem()).isEqualTo("maven");
        assertThat(components.get(0).purl()).isEqualTo("pkg:maven/org.apache.logging.log4j/log4j-core@2.14.1");

        assertThat(components.get(1).name()).isEqualTo("snakeyaml");
        assertThat(components.get(1).version()).isEqualTo("1.33");
    }

    @Test
    void shouldParseNpmPackageJsonDependencies(@TempDir Path tempDir) throws IOException {
        String packageJson = """
            {
              "name": "sample-frontend",
              "dependencies": {
                "lodash": "^4.17.15",
                "tar": "6.1.0"
              },
              "devDependencies": {
                "minimist": "1.2.5"
              }
            }
            """;
        Path pkgFile = tempDir.resolve("package.json");
        Files.writeString(pkgFile, packageJson);

        List<SbomComponent> components = sbomService.parsePackageJson(pkgFile, tempDir);

        assertThat(components).hasSize(3);
        assertThat(components.stream().map(SbomComponent::name)).containsExactlyInAnyOrder("lodash", "tar", "minimist");
    }

    @Test
    void shouldParsePythonRequirementsTxt(@TempDir Path tempDir) throws IOException {
        String requirements = """
            # Dependencies
            urllib3==1.26.4
            django==3.2.12
            """;
        Path reqFile = tempDir.resolve("requirements.txt");
        Files.writeString(reqFile, requirements);

        List<SbomComponent> components = sbomService.parseRequirementsTxt(reqFile, tempDir);

        assertThat(components).hasSize(2);
        assertThat(components.get(0).name()).isEqualTo("urllib3");
        assertThat(components.get(0).version()).isEqualTo("1.26.4");
        assertThat(components.get(0).ecosystem()).isEqualTo("pypi");
    }

    @Test
    void shouldDetectSupplyChainCvesInEndToEndScan(@TempDir Path tempDir) throws IOException {
        // Create vulnerable Maven pom.xml
        String pomXml = """
            <project>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.logging.log4j</groupId>
                        <artifactId>log4j-core</artifactId>
                        <version>2.14.1</version>
                    </dependency>
                </dependencies>
            </project>
            """;
        Files.writeString(tempDir.resolve("pom.xml"), pomXml);

        // Create vulnerable package.json
        String pkgJson = """
            {
              "dependencies": {
                "lodash": "4.17.15"
              }
            }
            """;
        Files.writeString(tempDir.resolve("package.json"), pkgJson);

        UUID jobId = UUID.randomUUID();
        SbomReport report = sbomService.generateSbom(tempDir, jobId, "TestProject");

        assertThat(report.totalDependencies()).isEqualTo(2);
        assertThat(report.vulnerableDependencies()).isGreaterThanOrEqualTo(2);
        assertThat(report.detectedVulnerabilities().stream().map(v -> v.cveId()))
                .contains("CVE-2021-44228", "CVE-2019-10744");

        // Verify CycloneDX serialization
        String cyclonedxJson = sbomService.exportCycloneDxJson(report);
        assertThat(cyclonedxJson).contains("\"bomFormat\": \"CycloneDX\"");
        assertThat(cyclonedxJson).contains("\"specVersion\": \"1.5\"");
        assertThat(cyclonedxJson).contains("log4j-core");
        assertThat(cyclonedxJson).contains("lodash");
    }
}
