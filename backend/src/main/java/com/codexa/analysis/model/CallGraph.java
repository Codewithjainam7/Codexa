package com.codexa.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Directed Interprocedural Call Graph Representation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallGraph {

    public record Node(String className, String methodName, int line, String filePath) {}
    public record Edge(Node caller, Node callee, int callLine) {}

    @Builder.Default
    private Set<Node> nodes = new HashSet<>();

    @Builder.Default
    private List<Edge> edges = new ArrayList<>();

    @Builder.Default
    private Map<String, List<String>> adjacencyList = new HashMap<>();

    public void addEdge(Node caller, Node callee, int line) {
        nodes.add(caller);
        nodes.add(callee);
        edges.add(new Edge(caller, callee, line));

        String callerKey = caller.className() + "#" + caller.methodName();
        String calleeKey = callee.className() + "#" + callee.methodName();
        adjacencyList.computeIfAbsent(callerKey, k -> new ArrayList<>()).add(calleeKey);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return edges.size();
    }
}
