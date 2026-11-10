const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

/**
 * CycloneDX v1.5 Software Bill of Materials (SBOM) JSON Generator.
 * Conforms to OWASP CycloneDX v1.5 specification.
 */
class CycloneDxSbomGenerator {
  constructor(projectDir = process.cwd()) {
    this.projectDir = projectDir;
  }

  /**
   * Generates a CycloneDX v1.5 SBOM JSON structure.
   *
   * @param {Object} metadata Root application metadata
   * @param {Array} dependencies Array of discovered dependencies
   * @param {Array} findings Optional security findings to correlate
   * @returns {Object} CycloneDX v1.5 JSON Document
   */
  generateSbom(metadata = {}, dependencies = [], findings = []) {
    const serialNumber = `urn:uuid:${crypto.randomUUID()}`;
    const timestamp = new Date().toISOString();

    const components = dependencies.map((dep) => {
      const purl = dep.purl || (dep.group 
        ? `pkg:maven/${dep.group}/${dep.name}@${dep.version || "latest"}`
        : `pkg:npm/${dep.name}@${dep.version || "latest"}`);

      const comp = {
        type: "library",
        "bom-ref": purl,
        name: dep.name,
        version: dep.version || "1.0.0",
        purl,
        scope: dep.dev ? "optional" : "required"
      };

      if (dep.license) {
        comp.licenses = [
          {
            license: {
              id: dep.license
            }
          }
        ];
      }

      return comp;
    });

    const vulnerabilities = findings
      .filter((f) => f.ruleId === "CR-DEP-001" || f.cve)
      .map((f) => ({
        "bom-ref": `vuln-${crypto.randomUUID()}`,
        id: f.cve || f.title || "VULN-UNKNOWN",
        source: {
          name: "Codexa Vulnerability Intelligence",
          url: "https://codexa.dev/advisories"
        },
        ratings: [
          {
            severity: (f.severity || "info").toLowerCase()
          }
        ],
        description: f.description || f.title,
        recommendation: f.remediation,
        affects: [
          {
            ref: f.purl || `pkg:npm/${f.package || "unknown"}`
          }
        ]
      }));

    return {
      bomFormat: "CycloneDX",
      specVersion: "1.5",
      serialNumber,
      version: 1,
      metadata: {
        timestamp,
        tools: [
          {
            vendor: "Codexa AI",
            name: "Codexa Security Scanner",
            version: "3.5.0"
          }
        ],
        component: {
          type: "application",
          name: metadata.name || "application",
          version: metadata.version || "1.0.0"
        }
      },
      components,
      vulnerabilities
    };
  }

  /**
   * Generates SBOM from package.json in projectDir.
   */
  generateFromPackageJson() {
    const pkgPath = path.join(this.projectDir, "package.json");
    if (!fs.existsSync(pkgPath)) {
      throw new Error(`package.json not found in ${this.projectDir}`);
    }

    const pkg = JSON.parse(fs.readFileSync(pkgPath, "utf-8"));
    const deps = [];

    const runtimeDeps = pkg.dependencies || {};
    for (const [name, version] of Object.entries(runtimeDeps)) {
      deps.push({
        name,
        version: version.replace(/[\^~>=<]/g, ""),
        dev: false
      });
    }

    const devDeps = pkg.devDependencies || {};
    for (const [name, version] of Object.entries(devDeps)) {
      deps.push({
        name,
        version: version.replace(/[\^~>=<]/g, ""),
        dev: true
      });
    }

    return this.generateSbom({ name: pkg.name, version: pkg.version }, deps);
  }
}

module.exports = {
  CycloneDxSbomGenerator
};
