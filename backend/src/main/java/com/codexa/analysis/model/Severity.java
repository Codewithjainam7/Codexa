package com.codexa.analysis.model;

public enum Severity {
    CRITICAL(1.00),
    HIGH(0.80),
    MEDIUM(0.55),
    LOW(0.25),
    INFO(0.10);

    private final double weight;

    Severity(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
