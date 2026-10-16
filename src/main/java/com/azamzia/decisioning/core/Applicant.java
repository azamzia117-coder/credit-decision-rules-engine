package com.azamzia.decisioning.core;

/**
 * Synthetic credit application. Deliberately contains no protected characteristics (age, sex, race,
 * marital status, national origin) or close proxies such as ZIP code.
 */
public record Applicant(
        String applicantId,
        int creditScore,
        long annualIncomeMinor,
        long monthlyDebtMinor,
        long requestedAmountMinor,
        int employmentMonths,
        int openDelinquencies,
        boolean bankruptcyLast7Years) {

    /** Facts available to rules. Names here are the only identifiers a rule may reference. */
    public java.util.Map<String, Double> facts() {
        double annualIncome = annualIncomeMinor / 100.0;
        double dti = annualIncome <= 0 ? Double.POSITIVE_INFINITY : (monthlyDebtMinor / 100.0 * 12) / annualIncome;
        double lti = annualIncome <= 0 ? Double.POSITIVE_INFINITY : (requestedAmountMinor / 100.0) / annualIncome;
        return java.util.Map.of(
                "creditScore", (double) creditScore,
                "annualIncome", annualIncome,
                "debtToIncome", dti,
                "loanToIncome", lti,
                "requestedAmount", requestedAmountMinor / 100.0,
                "employmentMonths", (double) employmentMonths,
                "openDelinquencies", (double) openDelinquencies,
                "bankruptcyLast7Years", bankruptcyLast7Years ? 1.0 : 0.0);
    }

    public static final java.util.Set<String> FACT_NAMES = java.util.Set.of(
            "creditScore", "annualIncome", "debtToIncome", "loanToIncome", "requestedAmount",
            "employmentMonths", "openDelinquencies", "bankruptcyLast7Years");
}
