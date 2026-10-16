package com.azamzia.decisioning.adapter.web;

import com.azamzia.decisioning.config.RuleSetRegistry;
import com.azamzia.decisioning.core.Applicant;
import com.azamzia.decisioning.core.Decision;
import com.azamzia.decisioning.core.DecisionEngine;
import com.azamzia.decisioning.core.ImpactAnalyzer;
import com.azamzia.decisioning.core.ImpactReport;
import com.azamzia.decisioning.core.RuleSet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DecisionController {

    private final RuleSetRegistry registry;

    public DecisionController(RuleSetRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/decisions")
    public Decision decide(@Valid @RequestBody ApplicationRequest request) {
        return new DecisionEngine(registry.active()).decide(request.toApplicant());
    }

    @GetMapping("/rulesets")
    public List<String> versions() {
        return registry.versions();
    }

    /** Replays the synthetic portfolio through the active and candidate rule sets. */
    @GetMapping("/rulesets/impact")
    public ImpactReport impact(@RequestParam String candidate) {
        RuleSet next = registry.find(candidate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown rule set " + candidate));
        return ImpactAnalyzer.compare(registry.active(), next, registry.syntheticPortfolio());
    }

    public record ApplicationRequest(
            @NotBlank String applicantId,
            @Min(300) @Max(850) int creditScore,
            @PositiveOrZero long annualIncomeMinor,
            @PositiveOrZero long monthlyDebtMinor,
            @PositiveOrZero long requestedAmountMinor,
            @PositiveOrZero int employmentMonths,
            @PositiveOrZero int openDelinquencies,
            boolean bankruptcyLast7Years) {

        Applicant toApplicant() {
            return new Applicant(applicantId, creditScore, annualIncomeMinor, monthlyDebtMinor,
                    requestedAmountMinor, employmentMonths, openDelinquencies, bankruptcyLast7Years);
        }
    }
}
