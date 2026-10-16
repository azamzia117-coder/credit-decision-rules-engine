package com.azamzia.decisioning.core;

import java.util.ArrayList;
import java.util.List;

/** Reads the synthetic portfolio file used for impact analysis and golden tests. */
public final class PortfolioCsv {

    private PortfolioCsv() {
    }

    public static List<Applicant> parse(String csv) {
        List<Applicant> out = new ArrayList<>();
        String[] lines = csv.split("\\R");
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) continue;
            String[] f = lines[i].split(",");
            out.add(new Applicant(f[0], Integer.parseInt(f[1]), Long.parseLong(f[2]), Long.parseLong(f[3]),
                    Long.parseLong(f[4]), Integer.parseInt(f[5]), Integer.parseInt(f[6]), Boolean.parseBoolean(f[7])));
        }
        return out;
    }
}
