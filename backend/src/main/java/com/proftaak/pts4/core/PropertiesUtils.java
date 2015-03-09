package com.proftaak.pts4.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Michon
 */
public class PropertiesUtils {
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
        if (PropertiesUtils.properties == null) {
            PropertiesUtils.properties = new Properties();

            // Read the default properties file.
            InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            if (inputStream != null) {
                try {
                    PropertiesUtils.properties.load(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new FileNotFoundException("property file '" + PROPERTIES_FILE + "' not found in the classpath");
            }

            // Read user overrides.
            inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(USER_PROPERTIES_FILE);
            if (inputStream != null) {
                try {
                    PropertiesUtils.properties.load(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return PropertiesUtils.properties;
    }
}
