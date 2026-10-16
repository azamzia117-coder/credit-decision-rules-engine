package com.azamzia.decisioning.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Replays a portfolio of applications through a baseline and a candidate rule set and reports every
 * decision that would change. This is the evidence a reviewer needs before approving a rule change.
 */
public final class ImpactAnalyzer {

    private ImpactAnalyzer() {
    }

    public static ImpactReport compare(RuleSet baseline, RuleSet candidate, List<Applicant> portfolio) {
        DecisionEngine before = new DecisionEngine(baseline);
        DecisionEngine after = new DecisionEngine(candidate);
        Map<Outcome, Long> baselineCounts = new EnumMap<>(Outcome.class);
        Map<Outcome, Long> candidateCounts = new EnumMap<>(Outcome.class);
        Map<String, Long> transitions = new TreeMap<>();
        List<ImpactReport.Flip> flips = new ArrayList<>();
        for (Applicant a : portfolio) {
            Decision b = before.decide(a);
            Decision c = after.decide(a);
            baselineCounts.merge(b.outcome(), 1L, Long::sum);
            candidateCounts.merge(c.outcome(), 1L, Long::sum);
            if (b.outcome() != c.outcome()) {
                transitions.merge(b.outcome() + "->" + c.outcome(), 1L, Long::sum);
                flips.add(new ImpactReport.Flip(a.applicantId(), b.outcome(), c.outcome(), c.reasonCodes()));
            }
        }
        return new ImpactReport(baseline.version(), candidate.version(), portfolio.size(),
                baselineCounts, candidateCounts, transitions, flips);
    }
}
