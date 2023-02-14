package com.dsv.datafactory.file.extraction.processor.util;

public class ConfigurationLoader {
    public static String getOrDefault(String envName, String defaultValue) {
        return getOrDefault(envName, defaultValue, false);
    }

    public static String getOrDefault(String envName, String defaultValue, Boolean isEnvValueSecretHidden) {
        String value = System.getenv(envName);

        String envValueToPrint = (isEnvValueSecretHidden || envName.toLowerCase().contains("pass"))
                ? "Hidden:****"
                : value;

        if (value == null) {
            System.out.println("Using default value " + defaultValue + " for: " + envName);
            return defaultValue;
        } else {
            System.out.println(envValueToPrint + " found for: " + envName + " or Else: " + defaultValue);
            return value;
        }
    }

    public static String getOrFail(String envName) {
        String value = System.getenv(envName);
        if (value == null) {
            throw new RuntimeException("Could not find env variable " + envName);
        } else {
            return value;
        }
    }
}
