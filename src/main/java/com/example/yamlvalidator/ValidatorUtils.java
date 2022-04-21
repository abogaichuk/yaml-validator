package com.example.yamlvalidator;

import org.apache.commons.cli.CommandLine;

public class ValidatorUtils {
    public static boolean needHelp(CommandLine cmd) {
        return cmd.getOptions().length == 0 || cmd.hasOption("h");
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
