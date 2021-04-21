package com.treta.bot.util;

public class StringUtils {

    public static String nullIsBlank (String string) {
        return string == null ? "" : ": " + string;
    }
}
