package com.codexa.analysis.pipeline;

import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Taint Analysis Engine:
 * Performs inter-statement taint tracking within methods, tracing untrusted HTTP parameters
 * through assignments and operations to detect unsafe reachability in critical sinks.
 */
@Service
public class TaintAnalysisEngine {

    public record TaintSource(String paramName, String sourceAnnotation, int line) {}
    public record TaintPropagationStep(String fromVar, String toVar, int line, String expression) {}
    public record TaintSinkHit(String sinkType, String targetMethod, int line, String evidence, List<String> taintTrace) {}

    private static final Set<String> SOURCE_ANNOTATIONS = Set.of(
            "RequestParam", "PathVariable", "RequestBody", "RequestHeader", "ModelAttribute"
    );

    private static final Map<String, String> CRITICAL_SINKS = Map.of(
            "executeQuery", "SQL_INJECTION",
            "executeUpdate", "SQL_INJECTION",
            "createNativeQuery", "SQL_INJECTION",
            "exec", "COMMAND_INJECTION",
            "start", "COMMAND_INJECTION",
            "readObject", "DESERIALIZATION"
    );

    public List<TaintSinkHit> analyzeMethodTaint(ParsedJavaFile parsedFile) {
        List<TaintSinkHit> sinkHits = new ArrayList<>();
        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return sinkHits;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            Set<String> taintedVariables = new HashSet<>();
            List<String> traceLog = new ArrayList<>();

            // 1. Identify Ingress Taint Sources
            for (Parameter param : method.getParameters()) {
                boolean hasSourceAnnotation = param.getAnnotations().stream()
                        .anyMatch(a -> SOURCE_ANNOTATIONS.contains(a.getNameAsString()));
                if (hasSourceAnnotation) {
                    String paramName = param.getNameAsString();
                    taintedVariables.add(paramName);
                    int line = param.getRange().map(r -> r.begin.line).orElse(1);
                    traceLog.add(String.format("Taint Source [L%d]: Parameter '%s' marked with ingress annotation", line, paramName));
                }
            }

            if (taintedVariables.isEmpty()) {
                return;
            }

            // 2. Track Variable Propagation
            method.findAll(VariableDeclarator.class).forEach(varDecl -> {
                varDecl.getInitializer().ifPresent(init -> {
                    String assignedVar = varDecl.getNameAsString();
                    boolean readsTainted = taintedVariables.stream().anyMatch(t -> init.toString().contains(t));
                    if (readsTainted) {
                        taintedVariables.add(assignedVar);
                        int line = varDecl.getRange().map(r -> r.begin.line).orElse(1);
                        traceLog.add(String.format("Taint Propagate [L%d]: Variable '%s' derived from tainted expression '%s'", line, assignedVar, init));
                    }
                });
            });

            // 3. Trace into Critical Execution Sinks
            method.findAll(MethodCallExpr.class).forEach(call -> {
                String methodName = call.getNameAsString();
                if (CRITICAL_SINKS.containsKey(methodName)) {
                    for (int i = 0; i < call.getArguments().size(); i++) {
                        String argStr = call.getArgument(i).toString();
                        boolean argumentIsTainted = taintedVariables.stream().anyMatch(argStr::contains);
                        if (argumentIsTainted) {
                            int line = call.getRange().map(r -> r.begin.line).orElse(1);
                            List<String> currentTrace = new ArrayList<>(traceLog);
                            currentTrace.add(String.format("Taint Sink Reached [L%d]: Invocated sink '%s' with tainted argument '%s'", line, methodName, argStr));

                            sinkHits.add(new TaintSinkHit(
                                    CRITICAL_SINKS.get(methodName),
                                    methodName,
                                    line,
                                    call.toString(),
                                    currentTrace
                            ));
                        }
                    }
                }
            });
        });

        return sinkHits;
    }
}
