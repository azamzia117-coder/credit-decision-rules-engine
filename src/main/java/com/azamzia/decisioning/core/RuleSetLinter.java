package com.azamzia.decisioning.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Governance checks applied to every rule set before it can be loaded:
 * <ul>
 *   <li>rules may only reference known facts;</li>
 *   <li>rules may not reference protected characteristics or common proxies (fair-lending hygiene);</li>
 *   <li>at least one DECLINE rule must exist and every rule needs a human-readable reason, which is what
 *       an adverse-action notice would be built from.</li>
 * </ul>
 * This is an engineering illustration, not legal guidance.
 */
public final class RuleSetLinter {

    private static final Set<String> PROHIBITED = Set.of(
            "age", "dateofbirth", "sex", "gender", "race", "ethnicity", "religion", "maritalstatus",
            "nationalorigin", "zip", "zipcode", "postalcode");

    private RuleSetLinter() {
    }

    public static List<String> lint(RuleSet ruleSet) {
        List<String> problems = new ArrayList<>();
        boolean hasDecline = false;
        for (Rule rule : ruleSet.rules()) {
            if (rule.outcome() == Outcome.DECLINE) hasDecline = true;
            for (Condition c : rule.conditions()) {
                String normalized = c.fact().toLowerCase(Locale.ROOT);
                if (PROHIBITED.contains(normalized)) {
                    problems.add(rule.code() + ": references prohibited attribute '" + c.fact() + "'");
                } else if (!Applicant.FACT_NAMES.contains(c.fact())) {
                    problems.add(rule.code() + ": unknown fact '" + c.fact() + "'");
                }
            }
            if (rule.outcome() == Outcome.APPROVE) {
                problems.add(rule.code() + ": rules may only REFER or DECLINE; APPROVE is the default");
            }
        }
        if (!hasDecline) problems.add("rule set has no DECLINE rules");
        return problems;
    }
}
