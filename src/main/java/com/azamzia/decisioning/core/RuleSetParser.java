package com.azamzia.decisioning.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the plain-text rule format kept under version control, for example:
 *
 * <pre>
 * version: 2026.1
 * D01 | DECLINE | creditScore &lt; 580 | Credit score below program minimum
 * R03 | REFER   | debtToIncome &gt; 0.43 and creditScore &lt; 700 | High DTI with near-prime score
 * </pre>
 *
 * Every problem is reported with its line number so a bad rule change fails review instead of production.
 */
public final class RuleSetParser {

    private static final Pattern COMPARISON =
            Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9]*)\\s*(<=|>=|==|!=|<|>)\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");

    private RuleSetParser() {
    }

    public static RuleSet parse(String text) {
        String version = null;
        List<Rule> rules = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            int n = i + 1;
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.toLowerCase(Locale.ROOT).startsWith("version:")) {
                version = line.substring("version:".length()).strip();
                continue;
            }
            String[] parts = line.split("\\|");
            if (parts.length != 4) {
                errors.add("line " + n + ": expected 'CODE | OUTCOME | CONDITIONS | REASON'");
                continue;
            }
            String code = parts[0].strip();
            String reason = parts[3].strip();
            Outcome outcome;
            try {
                outcome = Outcome.valueOf(parts[1].strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                errors.add("line " + n + ": unknown outcome '" + parts[1].strip() + "'");
                continue;
            }
            if (!codes.add(code)) errors.add("line " + n + ": duplicate rule code " + code);
            if (reason.isEmpty()) errors.add("line " + n + ": reason text is required");
            List<Condition> conditions = new ArrayList<>();
            for (String clause : parts[2].split("(?i)\\s+and\\s+")) {
                Matcher m = COMPARISON.matcher(clause);
                if (!m.matches()) {
                    errors.add("line " + n + ": cannot parse condition '" + clause.strip() + "'");
                    continue;
                }
                conditions.add(new Condition(m.group(1), m.group(2), Double.parseDouble(m.group(3))));
            }
            rules.add(new Rule(code, outcome, List.copyOf(conditions), reason));
        }
        if (version == null) errors.add("missing 'version:' header");
        if (!errors.isEmpty()) throw new RuleSetException(errors);
        RuleSet ruleSet = new RuleSet(version, List.copyOf(rules));
        List<String> lint = RuleSetLinter.lint(ruleSet);
        if (!lint.isEmpty()) throw new RuleSetException(lint);
        return ruleSet;
    }
}
