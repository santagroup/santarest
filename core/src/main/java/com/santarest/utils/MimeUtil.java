package com.santarest.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public final class MimeUtil {
    private static final Pattern CHARSET = Pattern.compile("\\Wcharset=([^\\s;]+)", CASE_INSENSITIVE);

    /**
     * Parse the MIME type from a {@code Content-Type} header value or default to "UTF-8".
     *
     * @deprecated Use {@link #parseCharset(String, String)}.
     */
    @Deprecated
    public static String parseCharset(String mimeType) {
        return parseCharset(mimeType, "UTF-8");
    }

    /**
     * Parse the MIME type from a {@code Content-Type} header value.
     */
    public static String parseCharset(String mimeType, String defaultCharset) {
        Matcher match = CHARSET.matcher(mimeType);
        if (match.find()) {
            return match.group(1).replaceAll("[\"\\\\]", "");
        }
        return defaultCharset;
    }

    private MimeUtil() {
        // No instances.
    }
}
