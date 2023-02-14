package com.dsv.datafactory.file.extraction.processor.logging;

import com.dsv.logger.ECSLogger;

import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrDefault;

public class ECSLoggerProvider {
    public static final String LOG_APP_NAME_SDD_ENV_VAR = "LOG_APP_NAME_SDD";
    public static final String LOG_APP_ID_SDD_ENV_VAR = "LOG_APP_ID_SDD";
    public static final String LOG_LEVEL_ENV_VAR = "LOG_LEVEL";

    public static ECSLogger getLogger(String className) {
        return getLogger(className, ECSLogger.Level.valueOf(getOrDefault(LOG_LEVEL_ENV_VAR, "WARN")));
    }
    public static ECSLogger getLogger(String className, ECSLogger.Level level) {
        ECSLogger logger = new ECSLogger(
                getOrDefault(LOG_APP_NAME_SDD_ENV_VAR, "aifactory"),
                getOrDefault(LOG_APP_ID_SDD_ENV_VAR, "5389"),
                className);
        logger.setLevel(className, level);
        return  logger;
    }
}