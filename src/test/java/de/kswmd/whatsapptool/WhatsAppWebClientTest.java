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
import de.kswmd.whatsapptool.contacts.ChatListBean;
import static de.kswmd.whatsapptool.contacts.ChatListBean.Type.CONTACT;
import de.kswmd.whatsapptool.contacts.Entity;
import de.kswmd.whatsapptool.contacts.MessageFileDatabase;
import de.kswmd.whatsapptool.quartz.ScheduleManager;
import de.kswmd.whatsapptool.selenium.WebDriverFactory;
import static de.kswmd.whatsapptool.selenium.WebDriverFactory.Browser.CHROME;
import de.kswmd.whatsapptool.text.MessageParser;
import de.kswmd.whatsapptool.utils.ChronoConstants;
import de.kswmd.whatsapptool.utils.PathResolver;
import de.kswmd.whatsapptool.utils.Settings;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.quartz.SimpleScheduleBuilder;
import org.xml.sax.SAXException;

/**
 *
 * @author Kai Denzel
 */
public class WhatsAppWebClientTest {

    private static Logger LOGGER;

    private static ScheduleManager scheduleManager;
    private static WebDriverFactory webDriverFactory;

    private static WebDriver driver;
    private static WhatsAppWebClient client;

    static {
        System.setProperty(MiscConstants.KEY_LOG_FILE_PATH, PathResolver.getJarFilePathOrWorkingDirectory().toString() + "/logs");
        LOGGER = LogManager.getLogger();
        Configurator.setLevel("de.kswmd.whatsapptool", org.apache.logging.log4j.Level.DEBUG);
    }

    public WhatsAppWebClientTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        scheduleManager = ScheduleManager.getInstance();
        webDriverFactory = new WebDriverFactory(true, CHROME);
        driver = webDriverFactory.createWebDriver();
        client = new WhatsAppWebClient(driver);
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
    public void testSendMessage() {
        for (int i = 0; i < 1; i++) {
            try {
                client.open();
                client.waitForTimeOut(5);
                String content = MessageParser.DEFAULT_PARSER.format("[file:logs/test.txt]").replaceAll("\n", WhatsAppHelper.SHIFT_ENTER);
                WhatsAppHelper.sendMessage(Settings.getInstance().getAdminPhoneNumber(), content, client);
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
            client.waitForTimeOut(ChronoConstants.DURATION_OF_5_SECONDS);
        }
    }

    // @Test
    public void testSendMessage2() {
        for (int i = 0; i < 1; i++) {
            try {
                client.open();
                client.waitForTimeOut(5);
                StringBuilder sb = new StringBuilder();
                sb.append("Aha was soll das ");
                sb.append(Emoji.SMILING_FACE_WITH_OPEN_MOUTH_AND_CLOSED_EYES.getSequence());
                sb.append("Blablkabluasodfhasdojfbo ");
                sb.append(Emoji.GRINNING_FACE_WITH_SMILING_EYES.getSequence());
                sb.append(" ☹️HA");
                WhatsAppHelper.sendMessage(Settings.getInstance().getAdminPhoneNumber(), sb.toString(), client);
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
            client.waitForTimeOut(ChronoConstants.DURATION_OF_5_SECONDS);
        }
    }

    //@Test
    public void testSendingEmojis() {
        Emoji[] emojis = Emoji.values();
        LOGGER.info(Arrays.toString(emojis));
        client.open(Settings.getInstance().getAdminPhoneNumber());
        try {

            StringBuilder sb = new StringBuilder();
            sb.append("Sende emojis : hier ein colon : da ein colon");
            for (Emoji e : emojis) {
                sb.append(e.getSequence());
            }
            client.setText(sb.toString(), Duration.ofSeconds(10));
            //client.send(3);
            assertTrue(true);
        } catch (Exception ex) {
            LOGGER.error("", ex);
            assertTrue(false);
        }

        client.waitForTimeOut(5);
    }

    @Test
    public void testMaintenanceChronJob() {
        client.open();
        client.waitForTimeOut(ChronoConstants.DURATION_OF_10_SECONDS);
        assertTrue(scheduleManager.start());
        scheduleManager.scheduleMaintenanceJob(client, SimpleScheduleBuilder.simpleSchedule());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            LOGGER.error("Error", ex);
        }
        assertTrue(scheduleManager.stop());
        client.waitForTimeOut(ChronoConstants.DURATION_OF_10_SECONDS);
    }

    //@Test
    public void testNotificationsXML() {
        try {
            client.open();
            MessageFileDatabase db = MessageFileDatabase.create(Settings.getInstance().getNotificationsXMLFile());
            db.loadEntities();
            List<Entity> entities = db.getEntities();
            StringBuilder errorMessage = new StringBuilder();
            for (Entity e : entities) {
                String id = e.getIdentifier();
                try {
                    client.search(id, Duration.ofSeconds(30));
                } catch (TimeoutWhatsAppWebException ex) {
                    java.util.logging.Logger.getLogger(WhatsAppWebClientTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                client.waitForTimeOut(Duration.ofSeconds(1));
                WebElement chatList = client.getChatList();

                List<ChatListBean> elements = WhatsAppHelper.generateFromWebElement(chatList).stream().filter(cbl -> cbl.getType().equals(CONTACT)).collect(Collectors.toList());
                assertTrue(!elements.isEmpty());
                ChatListBean clb = elements.get(0);
                client.clickElement(By.xpath("//div[@data-testid='" + clb.getListItemTestId() + "']"));
                client.waitForTimeOut(10);

            }
            assertTrue(true);
            assertEquals(errorMessage.length(), 0);
        } catch (SAXException ex) {
            LOGGER.error("Error", ex);
            assertTrue(false);
        } catch (IOException ex) {
            LOGGER.error("Error", ex);
            assertTrue(false);
        } catch (ParserConfigurationException ex) {
            LOGGER.error("Error", ex);
            assertTrue(false);
        } catch (XPathExpressionException ex) {
            LOGGER.error("Error", ex);
            assertTrue(false);
        } catch (ParseException ex) {
            LOGGER.error("Error", ex);
            assertTrue(false);
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.error("Error", ex);
            assertTrue(false);
        }
    }

    //@Test
    public void testMessage() throws NoSuchWhatsAppWebElementException {
        LocalDateTime now = LocalDateTime.now();
        try {
            client.open(Settings.getInstance().getAdminPhoneNumber());
            client.waitForTimeOut(10);
            WhatsAppHelper.sendMessage(Settings.getInstance().getAdminPhoneNumber(),
                    MessageParser.DEFAULT_PARSER.format("[file:messages/problemLog.txt]").replaceAll("\n", WhatsAppHelper.SHIFT_ENTER),
                    client);
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
        client.waitForTimeOut(ChronoConstants.DURATION_OF_10_SECONDS);
    }

}
