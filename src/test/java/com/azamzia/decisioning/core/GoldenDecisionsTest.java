package com.azamzia.decisioning.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guard: every application in the synthetic portfolio must keep the decision recorded in
 * {@code expected-decisions-v1.csv}. Any rule edit that changes an outcome fails here until the golden
 * file is consciously updated in the same pull request.
 */
class GoldenDecisionsTest {

    @Test
    void v1DecisionsMatchApprovedGoldenFile() {
        DecisionEngine engine = new DecisionEngine(RuleSetParser.parse(TestResources.read("/rules/v1.rules")));
        List<Applicant> portfolio = PortfolioCsv.parse(TestResources.read("/portfolio/synthetic-portfolio.csv"));
        Map<String, String> expected = new HashMap<>();
        for (String line : TestResources.read("/golden/expected-decisions-v1.csv").split("\\R")) {
            if (line.isBlank() || line.startsWith("applicant_id")) continue;
            String[] f = line.split(",", 2);
            expected.put(f[0], f[1]);
        }
        assertTrue(portfolio.size() >= 100, "portfolio should be large enough to be meaningful");
        assertEquals(portfolio.size(), expected.size());
        for (Applicant a : portfolio) {
            Decision d = engine.decide(a);
            String actual = d.outcome() + "," + String.join(";", d.reasonCodes());
            assertEquals(expected.get(a.applicantId()), actual, "decision changed for " + a.applicantId());
        }
    }
}
