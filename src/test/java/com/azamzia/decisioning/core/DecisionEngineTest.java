package com.azamzia.decisioning.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecisionEngineTest {

    private final DecisionEngine engine = new DecisionEngine(RuleSetParser.parse(TestResources.read("/rules/v1.rules")));

    // income $60,000/yr; monthly debt $1,500 => DTI 0.30; request $12,000 => LTI 0.20
    private static Applicant base(String id) {
        return new Applicant(id, 720, 6_000_000, 150_000, 1_200_000, 36, 0, false);
    }

    @Test
    void cleanApplicationIsApprovedWithNoReasons() {
        Decision d = engine.decide(base("A1"));
        assertEquals(Outcome.APPROVE, d.outcome());
        assertEquals(List.of(), d.reasonCodes());
        assertEquals("2026.1", d.ruleSetVersion());
    }

    @Test
    void declineOutranksReferAndOnlyDeclineReasonsAreReturned() {
        // score 560 (D01) + 6 months employment (R02)
        Applicant a = new Applicant("A2", 560, 6_000_000, 150_000, 1_200_000, 6, 0, false);
        Decision d = engine.decide(a);
        assertEquals(Outcome.DECLINE, d.outcome());
        assertEquals(List.of("D01"), d.reasonCodes());
    }

    @Test
    void compoundConditionNeedsBothClauses() {
        // DTI 0.46 (monthly debt $2,300) with score 690 -> R01 refers
        Applicant refer = new Applicant("A3", 690, 6_000_000, 230_000, 1_200_000, 36, 0, false);
        // same DTI with score 740 -> R01 does not fire
        Applicant approve = new Applicant("A4", 740, 6_000_000, 230_000, 1_200_000, 36, 0, false);
        assertEquals(Outcome.REFER, engine.decide(refer).outcome());
        assertEquals(List.of("R01"), engine.decide(refer).reasonCodes());
        assertEquals(Outcome.APPROVE, engine.decide(approve).outcome());
    }

    @Test
    void multipleDeclineReasonsAreAllReported() {
        Applicant a = new Applicant("A5", 550, 6_000_000, 300_000, 1_200_000, 36, 4, true);
        assertEquals(List.of("D01", "D02", "D03", "D04"), engine.decide(a).reasonCodes());
    }

    @Test
    void zeroIncomeIsTreatedAsInfiniteDebtToIncome() {
        Applicant a = new Applicant("A6", 750, 0, 10_000, 100_000, 36, 0, false);
        assertEquals(Outcome.DECLINE, engine.decide(a).outcome());
    }
}
