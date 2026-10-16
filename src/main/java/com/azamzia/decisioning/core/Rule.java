package com.azamzia.decisioning.core;

import java.util.List;
import java.util.Map;

/** Fires when all of its conditions are true (logical AND). */
public record Rule(String code, Outcome outcome, List<Condition> conditions, String reason) {

    public boolean fires(Map<String, Double> facts) {
        return conditions.stream().allMatch(c -> c.test(facts));
    }
}
