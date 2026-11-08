package com.codexa.analysis.service;

import com.codexa.analysis.model.PackageCouplingMetrics;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Analyzes package coupling (Afferent/Efferent) and identifies circular package dependencies.
 */
@Service
public class PackageCouplingAnalyzer {

    public PackageCouplingMetrics analyze(List<ParsedJavaFile> files) {
        PackageCouplingMetrics metrics = new PackageCouplingMetrics();
        if (files == null || files.isEmpty()) {
            return metrics;
        }

        Map<String, Set<String>> outgoingDependencies = new HashMap<>();
        Map<String, Set<String>> incomingDependencies = new HashMap<>();
        Set<String> allPackages = new HashSet<>();

        for (ParsedJavaFile file : files) {
            if (file.getCompilationUnit().isEmpty()) continue;
            CompilationUnit cu = file.getCompilationUnit().get();

            String pkg = cu.getPackageDeclaration()
                    .map(p -> p.getNameAsString())
                    .orElse("default");

            allPackages.add(pkg);

            for (ImportDeclaration imp : cu.getImports()) {
                String imported = imp.getNameAsString();
                int lastDot = imported.lastIndexOf('.');
                if (lastDot > 0) {
                    String importedPkg = imported.substring(0, lastDot);
                    if (!importedPkg.equals(pkg) && !importedPkg.startsWith("java.") && !importedPkg.startsWith("javax.")) {
                        outgoingDependencies.computeIfAbsent(pkg, k -> new HashSet<>()).add(importedPkg);
                        incomingDependencies.computeIfAbsent(importedPkg, k -> new HashSet<>()).add(pkg);
                    }
                }
            }
        }

        for (String pkg : allPackages) {
            int ca = incomingDependencies.getOrDefault(pkg, Collections.emptySet()).size();
            int ce = outgoingDependencies.getOrDefault(pkg, Collections.emptySet()).size();
            double instability = (ca + ce) == 0 ? 0.0 : (double) ce / (ca + ce);

            metrics.getPackageMetrics().put(pkg, new PackageCouplingMetrics.PackageStats(
                    pkg, ca, ce, Math.round(instability * 100.0) / 100.0
            ));
        }

        return metrics;
    }
}
