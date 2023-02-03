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

import de.kswmd.whatsapptool.WhatsAppHelper.Emoji;
import de.kswmd.whatsapptool.utils.PathResolver;
import de.kswmd.whatsapptool.utils.Settings;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 *
 * @author Kai Denzel
 */
public class WhatsAppClientTest {

    private static Logger LOGGER;

    private static WebDriver driver;
    private static WhatsAppClient client;

    static {
        System.setProperty("logFilePath", PathResolver.getJarFilePathOrWorkingDirectory().toString() + "/logs");
        LOGGER = LogManager.getLogger();
    }

    public WhatsAppClientTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        Settings settings = Settings.getInstance();
        String userDataDir = settings.getProfilePathChrome();
        /**
         * ChromeOptions options = new ChromeOptions();
         * options.addArguments("--user-data-dir=" + userDataDir); driver = new
         * ChromeDriver(options);*
         */
        FirefoxProfile profile = new FirefoxProfile(new File(settings.getProfilePathFirefox()));
        FirefoxOptions capabilities = new FirefoxOptions();
        capabilities.setProfile(profile);
        FirefoxOptions options = new FirefoxOptions(capabilities);
        options.setHeadless(false);
        driver = new FirefoxDriver(options);
        client = new WhatsAppClient(driver);
    }

    @AfterAll
    public static void tearDownClass() {
        driver.quit();
    }

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void tearDown() {

    }

    //@Test
    public void testSendingEmojis() {
        String emoji = Emoji.GRINNING_FACE.getSequence();
        LOGGER.info(emoji);
        client.open(Settings.getInstance().getAdminPhoneNumber());
        if (!client.waitTilTextIsAvailable(1)) {

            try {
                client.setText(emoji);
                Assertions.assertTrue(true);
            }
            catch (Exception ex) {
                LOGGER.error("", ex);
                Assertions.assertTrue(false);
            }
            client.waitForTimeOut(1);
        }
    }

}
