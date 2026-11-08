package com.codexa.analysis.pipeline;

import com.codexa.analysis.model.CallGraph;
import com.codexa.analysis.model.TaintGraph;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Propagates taint status across method invocation boundaries using the call graph.
 */
@Service
public class CrossFileTaintPropagator {

    public TaintGraph propagate(CallGraph callGraph, Set<String> initialTaintedMethods) {
        TaintGraph graph = new TaintGraph();
        if (callGraph == null || initialTaintedMethods == null) {
            return graph;
        }

        Queue<String> queue = new LinkedList<>(initialTaintedMethods);
        Set<String> visited = new HashSet<>(initialTaintedMethods);

        while (!queue.isEmpty()) {
            String currentMethod = queue.poll();
            List<String> callees = callGraph.getAdjacencyList().getOrDefault(currentMethod, Collections.emptyList());

            for (String callee : callees) {
                if (!visited.contains(callee)) {
                    visited.add(callee);
                    queue.add(callee);

                    TaintGraph.TaintNode fromNode = new TaintGraph.TaintNode("", "", currentMethod, "arg", 0);
                    TaintGraph.TaintNode toNode = new TaintGraph.TaintNode("", "", callee, "param", 0);
                    graph.addPropagation(fromNode, toNode, "INVOCATION");
                }
            }
        }

        return graph;
    }
}
