package com.azamzia.decisioning.core;

import java.util.Map;

/** A single comparison such as {@code debtToIncome > 0.43}. */
public record Condition(String fact, String operator, double value) {

    public boolean test(Map<String, Double> facts) {
        Double actual = facts.get(fact);
        if (actual == null) {
            throw new IllegalStateException("fact not provided: " + fact);
        }
        return switch (operator) {
            case "<" -> actual < value;
            case "<=" -> actual <= value;
            case ">" -> actual > value;
            case ">=" -> actual >= value;
            case "==" -> actual == value;
            case "!=" -> actual != value;
            default -> throw new IllegalStateException("unsupported operator " + operator);
        };
    }

    @Override
    public String toString() {
        return fact + " " + operator + " " + value;
    }
}
