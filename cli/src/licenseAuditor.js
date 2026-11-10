const fs = require("fs");
const path = require("path");

/**
 * Open Source Software (OSS) License Compliance Auditor
 * Classifies packages into Permissive, Weak Copyleft, and Strong Copyleft (GPL/AGPL).
 */
class LicenseAuditor {
  constructor(options = {}) {
    this.allowCopyleft = options.allowCopyleft || false;
    this.disallowedLicenses = new Set(options.disallowedLicenses || ["GPL-2.0", "GPL-3.0", "AGPL-3.0", "SSPL"]);
  }

  /**
   * Evaluates a license string for risk classification.
   */
  classifyLicense(licenseStr) {
    if (!licenseStr || licenseStr === "UNKNOWN") {
      return { type: "UNKNOWN", risk: "MEDIUM", copyleft: false };
    }

    const clean = licenseStr.trim().toUpperCase();

    if (/^(MIT|APACHE-2\.0|BSD-2-CLAUSE|BSD-3-CLAUSE|ISC|UNLICENSE|0BSD)$/i.test(clean)) {
      return { type: "PERMISSIVE", risk: "LOW", copyleft: false };
    }

    if (/^(LGPL-2\.1|LGPL-3\.0|MPL-2\.0|EPL-2\.0)$/i.test(clean)) {
      return { type: "WEAK_COPYLEFT", risk: "MEDIUM", copyleft: true };
    }

    if (/^(GPL-2\.0|GPL-3\.0|AGPL-3\.0|SSPL|EUPL)$/i.test(clean)) {
      return { type: "STRONG_COPYLEFT", risk: "CRITICAL", copyleft: true };
    }

    return { type: "PROPRIETARY_OR_CUSTOM", risk: "MEDIUM", copyleft: false };
  }

  /**
   * Audits package.json dependencies.
   */
  auditPackageJson(packageJsonPath) {
    if (!fs.existsSync(packageJsonPath)) {
      throw new Error(`File not found: ${packageJsonPath}`);
    }

    const pkg = JSON.parse(fs.readFileSync(packageJsonPath, "utf-8"));
    const results = {
      packageName: pkg.name || "root",
      declaredLicense: pkg.license || "UNLICENSED",
      totalDependencies: 0,
      violations: [],
      compliant: []
    };

    const nodeModulesDir = path.join(path.dirname(packageJsonPath), "node_modules");
    const allDeps = Object.keys({ ...pkg.dependencies, ...pkg.devDependencies });
    results.totalDependencies = allDeps.length;

    for (const dep of allDeps) {
      let depLicense = "UNKNOWN";
      const depPkgPath = path.join(nodeModulesDir, dep, "package.json");
      if (fs.existsSync(depPkgPath)) {
        try {
          const depPkg = JSON.parse(fs.readFileSync(depPkgPath, "utf-8"));
          depLicense = depPkg.license || (typeof depPkg.licenses === "object" ? depPkg.licenses[0]?.type : "UNKNOWN") || "UNKNOWN";
        } catch {
          // Ignore parse errors
        }
      }

      const classification = this.classifyLicense(depLicense);
      const entry = {
        name: dep,
        license: depLicense,
        classification
      };

      if (!this.allowCopyleft && classification.copyleft && classification.risk === "CRITICAL") {
        results.violations.push(entry);
      } else {
        results.compliant.push(entry);
      }
    }

    return results;
  }
}

module.exports = {
  LicenseAuditor
};
