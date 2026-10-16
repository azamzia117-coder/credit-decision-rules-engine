package com.azamzia.decisioning.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

final class TestResources {

    private TestResources() {
    }

    static String read(String path) {
        try (InputStream in = TestResources.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("missing resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
