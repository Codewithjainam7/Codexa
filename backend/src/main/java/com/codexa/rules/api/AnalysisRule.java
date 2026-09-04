package com.codexa.rules.api;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;

import java.util.List;

public interface AnalysisRule {

    String getRuleId();

    String getName();

    Category getCategory();

    Severity getSeverity();

    Confidence getDefaultConfidence();

    String getOwaspMapping();

    List<RuleFinding> evaluate(RuleContext context);
}
