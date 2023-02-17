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
package de.kswmd.whatsapptool.contacts;

import de.kswmd.whatsapptool.MiscConstants;
import de.kswmd.whatsapptool.utils.PathResolver;
import de.kswmd.whatsapptool.utils.Settings;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Kai Denzel
 */
public class FileMessageDatabaseTest {

    private static final Logger LOGGER;

    static {
        System.setProperty(MiscConstants.KEY_LOG_FILE_PATH, PathResolver.getJarFilePathOrWorkingDirectory().toString() + "/logs");
        LOGGER = LogManager.getLogger();
    }

    private static MessageFileDatabase fileMessageDatabase;

    public FileMessageDatabaseTest() {
    }

    @BeforeAll
    public static void setUpClass() {

        try {
            fileMessageDatabase = MessageFileDatabase.create(Settings.getInstance().getNotificationsXMLFile());
        } catch (SAXException ex) {
            LOGGER.error("", ex);
            assertTrue(false);
        } catch (IOException ex) {
            LOGGER.error("", ex);
            assertTrue(false);
        } catch (ParserConfigurationException ex) {
            LOGGER.error("", ex);
            assertTrue(false);
        }
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of validateXMLSchema method, of class FileMessageDatabase.
     */
    @Test
    public void testValidateXMLSchema() {
        Settings settings = Settings.getInstance();
        String xmlPath = settings.getNotificationsXMLFile();
        try {
            fileMessageDatabase.validateXMLSchema(xmlPath);
        } catch (Exception ex) {
            LOGGER.error("", ex);
            assertTrue(false);
        }
        assertTrue(true);
    }

    @Test
    public void getEntities() {
        List<Entity> entities = fileMessageDatabase.getEntities();
        LOGGER.info(entities);
    }

}
