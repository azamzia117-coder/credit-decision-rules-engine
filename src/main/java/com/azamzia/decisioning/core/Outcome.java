package com.azamzia.decisioning.core;

/** Ordered by severity: the most severe outcome triggered by any rule wins. */
public enum Outcome {
    APPROVE,
    REFER,
    DECLINE
}
