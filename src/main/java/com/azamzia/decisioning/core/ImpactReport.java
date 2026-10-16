package com.azamzia.decisioning.core;

import java.util.List;
import java.util.Map;

public record ImpactReport(String baselineVersion, String candidateVersion, int applications,
                           Map<Outcome, Long> baselineOutcomes, Map<Outcome, Long> candidateOutcomes,
                           Map<String, Long> transitions, List<Flip> flips) {

    public record Flip(String applicantId, Outcome before, Outcome after, List<String> newReasonCodes) { }

    public long changed() {
        return flips.size();
    }
}
