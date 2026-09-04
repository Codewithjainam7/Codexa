package com.codexa.analysis.model;

public enum ProductionVerdict {
    REVIEW_COMPLETE("Review complete / low detected risk"),
    GENERALLY_PROMISING("Generally promising / review findings"),
    NEEDS_URGENT_FIXES("Needs fixes before production"),
    NOT_READY("Not ready");

    private final String description;

    ProductionVerdict(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
