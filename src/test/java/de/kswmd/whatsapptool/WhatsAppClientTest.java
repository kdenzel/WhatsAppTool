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
import de.kswmd.whatsapptool.utils.PathResolver;
import de.kswmd.whatsapptool.utils.Settings;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.quartz.SimpleScheduleBuilder;
import org.xml.sax.SAXException;

/**
 *
 * @author Kai Denzel
 */
public class WhatsAppClientTest {
    
    private static Logger LOGGER;
    
    private static ScheduleManager scheduleManager;
    private static WebDriverFactory webDriverFactory;
    
    private static WebDriver driver;
    private static WhatsAppClient client;
    
    static {
        System.setProperty(MiscConstants.KEY_LOG_FILE_PATH, PathResolver.getJarFilePathOrWorkingDirectory().toString() + "/logs");
        LOGGER = LogManager.getLogger();
    }
    
    public WhatsAppClientTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
        scheduleManager = ScheduleManager.getInstance();
        webDriverFactory = new WebDriverFactory(true, CHROME);
        driver = webDriverFactory.createWebDriver();
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
        if (client.waitTilTextIsAvailable(100)) {
            
            try {
                client.setText(emoji);
                //client.send(3);
                assertTrue(true);
            } catch (Exception ex) {
                LOGGER.error("", ex);
                assertTrue(false);
            }
            client.waitForTimeOut(1);
        } else {
            assertTrue(false);
        }
    }

    //@Test
    public void testMaintenanceChronJob() {
        assertTrue(scheduleManager.start());
        scheduleManager.scheduleMaintenanceJob(client, SimpleScheduleBuilder.simpleSchedule());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            LOGGER.error("Error", ex);
        }
        assertTrue(scheduleManager.stop());
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
                client.search(id, Duration.ofSeconds(30));
                client.waitForTimeOut(Duration.ofSeconds(1));
                Optional<WebElement> chatList = client.getChatList();
                if (chatList.isPresent()) {
                    List<ChatListBean> elements = WhatsAppHelper.generateFromWebElement(chatList.get()).stream().filter(cbl -> cbl.getType().equals(CONTACT)).collect(Collectors.toList());
                    assertTrue(!elements.isEmpty());
                    ChatListBean clb = elements.get(0);
                    client.clickElement(By.xpath("//div[@data-testid='" + clb.getListItemTestId() + "']"));
                    client.waitForTimeOut(10);
                }
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
        }
    }
    
}
