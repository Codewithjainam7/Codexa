package com.codexa.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Cross-file Taint Propagation Graph.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaintGraph {

    public record TaintNode(String file, String className, String method, String variable, int line) {}
    public record TaintEdge(TaintNode from, TaintNode to, String operation) {}

    @Builder.Default
    private Set<TaintNode> taintSources = new HashSet<>();

    @Builder.Default
    private Set<TaintNode> taintSinks = new HashSet<>();

    @Builder.Default
    private List<TaintEdge> propagationPath = new ArrayList<>();

    public void addPropagation(TaintNode source, TaintNode destination, String operation) {
        propagationPath.add(new TaintEdge(source, destination, operation));
    }
}
