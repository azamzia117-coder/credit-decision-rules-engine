package com.azamzia.decisioning.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Evaluates every rule, then returns the most severe outcome and the reasons that produced it. */
public class DecisionEngine {

    private final RuleSet ruleSet;

    public DecisionEngine(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    public Decision decide(Applicant applicant) {
        Map<String, Double> facts = applicant.facts();
        List<Rule> fired = ruleSet.rules().stream().filter(r -> r.fires(facts)).toList();
        Outcome outcome = fired.stream().map(Rule::outcome).max(Enum::compareTo).orElse(Outcome.APPROVE);
        List<String> codes = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        for (Rule r : fired) {
            if (r.outcome() == outcome) {
                codes.add(r.code());
                reasons.add(r.reason());
            }
        }
        return new Decision(applicant.applicantId(), outcome, List.copyOf(codes), List.copyOf(reasons), ruleSet.version());
    }

    public RuleSet ruleSet() {
        return ruleSet;
    }
}
