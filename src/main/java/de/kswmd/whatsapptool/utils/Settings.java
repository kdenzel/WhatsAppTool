/*
 * The MIT License
 *
 * Copyright 2023 Kai Denzel.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.kswmd.whatsapptool.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kai Denzel
 */
public final class Settings {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String KEY_PROFILE_PATH_CHROME = "profile_path_chrome";
    public static final String KEY_PROFILE_PATH_FIREFOX = "profile_path_firefox";
    public static final String KEY_NOTIFICATION_FILE = "notification_file";
    public static final String KEY_ADMIN_PHONE = "admin_phone";

    private static Settings instance;

    private final Properties properties = new Properties();
    private final String configFilePath;
    private final String configFileName = "settings.properties";
    private final String configFilePathWithName;

    private Settings() {
        configFilePath = PathResolver.getConfigDir().toString();
        configFilePathWithName = configFilePath + "/" + configFileName;
        try (InputStream input = new FileInputStream(configFilePathWithName)) {
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.warn("Could not load settings, use default values instead...");
            LOGGER.trace("Error loading settings", ex);
        }
        writeDefaultValues();
        save();
        printProperties();
    }

    public static final synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private void writeDefaultValues() {
        writeDefaultValueIfNotPresent(KEY_PROFILE_PATH_CHROME, PathResolver.getDefaultChromeBrowserProfileDir().toString());
        writeDefaultValueIfNotPresent(KEY_PROFILE_PATH_FIREFOX, PathResolver.getDefaultFirefoxBrowserProfileDir().toString());
        writeDefaultValueIfNotPresent(KEY_ADMIN_PHONE, "+49...");
        writeDefaultValueIfNotPresent(KEY_NOTIFICATION_FILE, PathResolver.getConfigDir() + "/notifications.xml");
    }

    private void writeDefaultValueIfNotPresent(String key, String value) {
        if (!properties.containsKey(key)) {
            properties.put(key, value);
        }
    }

    private void printProperties() {
        properties.forEach((key, value) -> LOGGER.info("Key : " + key + ", Value : " + value));
    }

    public synchronized String getProfilePathChrome() {
        return properties.getProperty(KEY_PROFILE_PATH_CHROME);
    }

    public synchronized String getProfilePathFirefox() {
        return properties.getProperty(KEY_PROFILE_PATH_CHROME);
    }

    public synchronized String getNotificationsXMLFile() {
        return properties.getProperty(KEY_NOTIFICATION_FILE);
    }

    public synchronized String getAdminPhoneNumber() {
        return properties.getProperty(KEY_ADMIN_PHONE);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public synchronized void save() {
        new File(configFilePath).mkdirs();
        try {
            new File(configFilePathWithName).createNewFile();
        } catch (IOException ex) {
            LOGGER.error("Failed to create settings...", ex);
        }
        try (OutputStream output = new FileOutputStream(configFilePathWithName)) {
            properties.store(output, null);
        } catch (IOException io) {
            LOGGER.error("Failed to write settings...", io);
        }
    }
}
