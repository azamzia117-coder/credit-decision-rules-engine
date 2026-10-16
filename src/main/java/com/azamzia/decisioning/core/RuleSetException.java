package com.azamzia.decisioning.core;

import java.util.List;

public class RuleSetException extends RuntimeException {

    private final List<String> problems;

    public RuleSetException(List<String> problems) {
        super(String.join("; ", problems));
        this.problems = List.copyOf(problems);
    }

    public List<String> problems() {
        return problems;
    }
}
