package com.codexa.analysis.model;

public enum Confidence {
    HIGH(1.00),
    MEDIUM(0.75),
    LOW(0.50);

    private final double weight;

    Confidence(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
