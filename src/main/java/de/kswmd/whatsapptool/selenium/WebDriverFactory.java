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
package de.kswmd.whatsapptool.selenium;

import de.kswmd.whatsapptool.utils.Settings;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 *
 * @author Kai Denzel
 */
public final class WebDriverFactory {

    public enum Browser {
        CHROME,
        CHROMIUM,
        FIREFOX
    }

    private static final Logger LOGGER = LogManager.getLogger();

    private final boolean withGui;
    private final Browser browser;

    public WebDriverFactory(final boolean withGui, final Browser browser) {
        this.withGui = withGui;
        this.browser = browser;
    }

    public synchronized WebDriver createWebDriver() {
        return createWebDriver(browser);
    }

    public synchronized WebDriver createWebDriver(Browser browser) {
        WebDriver webDriver;
        switch (browser) {
            case FIREFOX:
                LOGGER.info("Installing driver for chromium browser.");
                WebDriverManager.firefoxdriver().setup();
                webDriver = createFirefoxWebDriver();
                break;
            case CHROME:
                LOGGER.info("Installing driver for chrome browser.");
                WebDriverManager.chromedriver().setup();
            case CHROMIUM:
                LOGGER.info("Installing driver for chromium browser.");
                WebDriverManager.chromiumdriver().setup();
            default:
                webDriver = createChromeWebDriver();
                break;
        }

        return webDriver;
    }

    private synchronized WebDriver createChromeWebDriver() {
        Settings settings = Settings.getInstance();
        String userDataDir = settings.getProfilePathChrome();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + userDataDir);
        if (!withGui) {
            options.addArguments("--headless", "--disable-gpu", "--nogpu", "--window-size=1920,1080", "--ignore-certificate-errors", "--no-sandbox", "--enable-javascript");
            //options.addArguments("--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        }
        WebDriver driver = new ChromeDriver(options);
        Object userAgent = ((ChromeDriver) driver).executeScript("return navigator.userAgent;");
        LOGGER.info("user-agent=" + userAgent);
        return driver;
    }

    private synchronized WebDriver createFirefoxWebDriver() {
        Settings settings = Settings.getInstance();
        String userDataDir = settings.getProfilePathFirefox();
        LOGGER.debug("user-data-dir=" + userDataDir);
        //ProfilesIni listProfiles = new ProfilesIni();
        //FirefoxProfile profile = listProfiles.getProfile("Selenium");
        //FirefoxBinary binary = new FirefoxBinary();  
        FirefoxProfile profile = new FirefoxProfile(new File(userDataDir));
        profile.setAcceptUntrustedCertificates(true);
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(!withGui);
        options.setProfile(profile);
        return new FirefoxDriver(options);
    }

}
