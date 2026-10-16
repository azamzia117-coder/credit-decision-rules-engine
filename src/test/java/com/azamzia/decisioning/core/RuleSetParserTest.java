package com.azamzia.decisioning.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleSetParserTest {

    @Test
    void parsesBundledRuleSets() {
        RuleSet v1 = RuleSetParser.parse(TestResources.read("/rules/v1.rules"));
        RuleSet v2 = RuleSetParser.parse(TestResources.read("/rules/v2.rules"));
        assertEquals("2026.1", v1.version());
        assertEquals(7, v1.rules().size());
        assertEquals(8, v2.rules().size());
        assertEquals(2, v1.rules().get(4).conditions().size());
    }

    @Test
    void reportsEveryProblemWithLineNumbers() {
        String text = """
                version: test
                D01 | DECLINE | creditScore < | Bad clause
                D01 | MAYBE   | creditScore < 500 | Unknown outcome
                X1 | REFER | creditScore < 500
                """;
        RuleSetException e = assertThrows(RuleSetException.class, () -> RuleSetParser.parse(text));
        assertTrue(e.problems().stream().anyMatch(p -> p.startsWith("line 2: cannot parse condition")), e.getMessage());
        assertTrue(e.problems().stream().anyMatch(p -> p.startsWith("line 3: unknown outcome")), e.getMessage());
        assertTrue(e.problems().stream().anyMatch(p -> p.startsWith("line 4: expected")), e.getMessage());
    }

    @Test
    void blocksRulesOnProtectedAttributesOrProxies() {
        String text = """
                version: test
                D01 | DECLINE | creditScore < 580 | Score
                D02 | DECLINE | zipCode == 46201 | Should never be allowed
                """;
        RuleSetException e = assertThrows(RuleSetException.class, () -> RuleSetParser.parse(text));
        assertTrue(e.getMessage().contains("prohibited attribute 'zipCode'"), e.getMessage());
    }

    @Test
    void rejectsUnknownFactsAndExplicitApproveRules() {
        String text = """
                version: test
                D01 | DECLINE | creditScore < 580 | Score
                A01 | APPROVE | creditScore > 800 | Super prime
                R01 | REFER   | netWorth < 1000 | Unknown fact
                """;
        RuleSetException e = assertThrows(RuleSetException.class, () -> RuleSetParser.parse(text));
        assertTrue(e.getMessage().contains("A01: rules may only REFER or DECLINE"), e.getMessage());
        assertTrue(e.getMessage().contains("R01: unknown fact 'netWorth'"), e.getMessage());
    }

    @Test
    void requiresVersionHeader() {
        assertThrows(RuleSetException.class,
                () -> RuleSetParser.parse("D01 | DECLINE | creditScore < 580 | Score"));
    }
}
