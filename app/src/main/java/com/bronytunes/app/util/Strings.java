package com.bronytunes.app.util;

/**
 * With <3 from JakeWharton/u2020
 */
public final class Strings {
    private Strings(){}

    public static boolean isBlank(CharSequence string) {
        return (string == null || string.toString().trim().length() == 0);
    }

    public static String valueOrDefault(String string, String defaultString) {
        return isBlank(string)? defaultString : string;
    }

    public static String truncateAt(String string, int length) {
        return string.length() > length? string.substring(0, length) : string;
    }
}
