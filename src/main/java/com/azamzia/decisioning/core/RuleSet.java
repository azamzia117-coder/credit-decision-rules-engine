package com.azamzia.decisioning.core;

import java.util.List;

public record RuleSet(String version, List<Rule> rules) {
}
