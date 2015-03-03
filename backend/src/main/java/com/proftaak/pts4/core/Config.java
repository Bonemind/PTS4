package com.proftaak.pts4.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Michon
 */
public class Config {
    /**
     * The properties file name.
     */
    private static final String PROPERTIES_FILE = "config.properties";

    /**
     * The user properties file name.
     */
    private static final String USER_PROPERTIES_FILE = "user.properties";

    /**
     * The properties object.
     */
    private static Properties properties;

    /**
     * Get the properties file.
     *
     * @return A Properties object.
     * @throws FileNotFoundException If the properties file
     */
    public static Properties getProperties() throws FileNotFoundException {
        if (Config.properties == null) {
            Config.properties = new Properties();

            // Read the default properties file.
            InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            if (inputStream != null) {
                try {
                    Config.properties.load(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new FileNotFoundException("property file '" + PROPERTIES_FILE + "' not found in the classpath");
            }

            // Read user overrides.
            inputStream = Config.class.getClassLoader().getResourceAsStream(USER_PROPERTIES_FILE);
            if (inputStream != null) {
                try {
                    Config.properties.load(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new FileNotFoundException("property file '" + USER_PROPERTIES_FILE + "' not found in the classpath");
            }
        }
        return Config.properties;
    }
}
