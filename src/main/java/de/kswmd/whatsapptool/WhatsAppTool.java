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
package de.kswmd.whatsapptool;

import de.kswmd.whatsapptool.cli.CLI;
import de.kswmd.whatsapptool.quartz.ScheduleManager;
import de.kswmd.whatsapptool.selenium.WebDriverFactory;
import de.kswmd.whatsapptool.selenium.WebDriverFactory.Browser;
import static de.kswmd.whatsapptool.selenium.WebDriverFactory.Browser.CHROME;
import de.kswmd.whatsapptool.utils.PathResolver;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 *
 * @author Kai Denzel
 */
public class WhatsAppTool {

    private static final Logger LOGGER;
    public static final String KEY_LOGFILE_PATH = "logFilePath";

    static {
        System.setProperty(KEY_LOGFILE_PATH, PathResolver.getJarFilePathOrWorkingDirectory().toString() + "/logs");
        LOGGER = LogManager.getLogger();
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> argsList = Arrays.asList(args);
        if (argsList.contains("--v")) {
            Configurator.setLevel("de.kswmd.whatsapptool", Level.DEBUG);
            LOGGER.debug("Setting level to debug.");
        }

        if (argsList.contains("--vv")) {
            Configurator.setLevel("de.kswmd.whatsapptool", Level.TRACE);
            LOGGER.trace("Setting level to trace.");
        }
        Browser browser = CHROME;
        if (argsList.contains("--browser")) {
            int index = argsList.indexOf("--browser");
            int nextIndex = index + 1;
            if (nextIndex < argsList.size()) {
                String arg = argsList.get(nextIndex);
                try {
                    browser = Browser.valueOf(arg.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    LOGGER.trace("Error for Argument " + arg, ex);
                    LOGGER.info(arg + " is an invalid value for argument browser. Possible values are " + Arrays.toString(Browser.values()));
                }
            } else {
                LOGGER.info("Missing value for argument browser. Possible values are " + Arrays.toString(Browser.values()));
            }
        }
        LOGGER.info("Use Browser " + browser);
        boolean withgui = argsList.contains("--withGui");
        WhatsAppTool whatsAppTool = new WhatsAppTool();
        whatsAppTool.initialize(withgui, browser);
    }

    public void printUserAgent() {
        ChromeDriver test = new ChromeDriver();
        Object userAgent = test.executeScript("return navigator.userAgent;");
        test.quit();
        LOGGER.debug(userAgent);
    }

    public void initialize(boolean withGui, Browser browser) {

        final ScheduleManager scheduleManager = ScheduleManager.getInstance();
        final WebDriverFactory webDriverFactory = new WebDriverFactory(withGui, browser);
        WebDriver driver = webDriverFactory.createWebDriver(browser);
        try {
            scheduleManager.start();
            WhatsAppClient client = new WhatsAppClient(driver);
            scheduleManager.scheduleMaintenanceJob(client);
            scheduleManager.pauseAllJobs();
            CLI cli = new CLI(client);
            cli.start();
        } catch (Exception ex) {
            LOGGER.fatal("The App crashed...", ex);
        } finally {
            scheduleManager.stop();
            driver.quit();
        }
    }
}
