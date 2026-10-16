package com.azamzia.decisioning.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpactAnalyzerTest {

    private final RuleSet v1 = RuleSetParser.parse(TestResources.read("/rules/v1.rules"));
    private final RuleSet v2 = RuleSetParser.parse(TestResources.read("/rules/v2.rules"));

    @Test
    void reportsEachDecisionThatChangesAndWhy() {
        List<Applicant> portfolio = List.of(
                // score 590: approved under v1 floor (580), declined under v2 floor (600)
                new Applicant("S1", 590, 6_000_000, 150_000, 1_200_000, 36, 0, false),
                // DTI 0.52, score 720: declined by v1 D03 (>0.50), approved under v2 (>0.55)
                new Applicant("S2", 720, 6_000_000, 260_000, 1_200_000, 36, 0, false),
                // one open delinquency: approved under v1, referred under new R04
                new Applicant("S3", 720, 6_000_000, 150_000, 1_200_000, 36, 1, false),
                // unchanged approve
                new Applicant("S4", 760, 9_000_000, 150_000, 1_200_000, 60, 0, false));

        ImpactReport report = ImpactAnalyzer.compare(v1, v2, portfolio);

        assertEquals(3L, report.changed());
        assertEquals(Long.valueOf(1), report.transitions().get("APPROVE->DECLINE"));
        assertEquals(Long.valueOf(1), report.transitions().get("DECLINE->APPROVE"));
        assertEquals(Long.valueOf(1), report.transitions().get("APPROVE->REFER"));
        assertEquals(List.of("D01"), report.flips().get(0).newReasonCodes());
        assertEquals(List.of("R04"), report.flips().get(2).newReasonCodes());
    }

    @Test
    void identicalRuleSetsProduceNoChanges() {
        List<Applicant> portfolio = PortfolioCsv.parse(TestResources.read("/portfolio/synthetic-portfolio.csv"));
        ImpactReport report = ImpactAnalyzer.compare(v1, v1, portfolio);
        assertEquals(0L, report.changed());
        assertEquals(portfolio.size(), report.applications());
    }
}
