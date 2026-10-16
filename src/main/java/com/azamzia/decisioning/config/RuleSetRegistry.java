package com.azamzia.decisioning.config;

import com.azamzia.decisioning.core.Applicant;
import com.azamzia.decisioning.core.PortfolioCsv;
import com.azamzia.decisioning.core.RuleSet;
import com.azamzia.decisioning.core.RuleSetParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads every {@code rules/*.rules} file at startup. A rule file that fails parsing or linting stops the
 * application from starting, so an invalid rule set can never serve decisions.
 */
@Component
public class RuleSetRegistry {

    private final Map<String, RuleSet> byVersion = new LinkedHashMap<>();
    private final String activeVersion;
    private final List<Applicant> portfolio;

    public RuleSetRegistry(@Value("${decisioning.active-version:2026.1}") String activeVersion) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (Resource r : resolver.getResources("classpath:rules/*.rules")) {
            RuleSet set = RuleSetParser.parse(read(r));
            byVersion.put(set.version(), set);
        }
        if (!byVersion.containsKey(activeVersion)) {
            throw new IllegalStateException("active rule set " + activeVersion + " not found; have " + byVersion.keySet());
        }
        this.activeVersion = activeVersion;
        this.portfolio = PortfolioCsv.parse(read(resolver.getResource("classpath:portfolio/synthetic-portfolio.csv")));
    }

    public RuleSet active() {
        return byVersion.get(activeVersion);
    }

    public Optional<RuleSet> find(String version) {
        return Optional.ofNullable(byVersion.get(version));
    }

    public List<String> versions() {
        return List.copyOf(byVersion.keySet());
    }

    public List<Applicant> syntheticPortfolio() {
        return portfolio;
    }

    private static String read(Resource r) {
        try {
            return r.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
