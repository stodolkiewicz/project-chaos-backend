package com.stodo.projectchaos.storage.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;

/**
 * Sanitizes filenames to prevent broken URLs and Path Traversal attacks.
 * Replaces spaces and special characters with underscores.
 */
public class FileNameSanitizer {
    public static String sanitize(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("Filename must not be empty");
        }

        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKD);
        String result = normalized.replaceAll("\\p{M}", "");

        result = result.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9.\\-_]", "");

        return result.toLowerCase();
    }
}
