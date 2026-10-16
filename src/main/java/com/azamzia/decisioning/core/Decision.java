package com.azamzia.decisioning.core;

import java.util.List;

/** Decision plus the reason codes behind it, ordered as the rules appear in the rule set. */
public record Decision(String applicantId, Outcome outcome, List<String> reasonCodes, List<String> reasons,
                       String ruleSetVersion) {
}
