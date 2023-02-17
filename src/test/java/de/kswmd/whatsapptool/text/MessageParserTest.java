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
package de.kswmd.whatsapptool.text;

import de.kswmd.whatsapptool.MiscConstants;
import de.kswmd.whatsapptool.contacts.Entity;
import de.kswmd.whatsapptool.contacts.Message;
import de.kswmd.whatsapptool.utils.PathResolver;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Kai Denzel
 */
public class MessageParserTest {

    private static final Logger LOGGER;
    
    static {
        System.setProperty(MiscConstants.KEY_LOG_FILE_PATH, PathResolver.getJarFilePathOrWorkingDirectory().toString() + "/logs");
        LOGGER = LogManager.getLogger();
    }
    
    public MessageParserTest() {
    }

    @BeforeAll
    public static void setUpClass() {
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
     * Test of format method, of class MessageParser.
     */
    @Test
    public void testFormat() {
        try {
            Configurator.setLevel("de.kswmd.whatsapptool", Level.TRACE);
            String text = Files.readString(Path.of("./logs/test.txt"));
            MessageParser instance = MessageParser.DEFAULT_PARSER;
            Entity e = new Entity();
            e.setIdentifier("Kai Denzel");
            Message m = new Message();
            m.setEntity(e);
            m.setCronExpression("0 * * ? * * *");
            m.setContent(text);
            String result = instance.format(m);
            System.out.println(result);
        } catch (Exception ex) {
            LOGGER.error("error",ex);
            assertTrue(false);
        }

    }

}
