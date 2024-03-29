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
package de.kswmd.whatsapptool.cli;

import de.kswmd.whatsapptool.NoSuchWhatsAppWebElementException;
import de.kswmd.whatsapptool.TimeoutWhatsAppWebException;
import de.kswmd.whatsapptool.WhatsAppWebClient;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kai Denzel
 */
public class CommandShowText extends Command {

    private static final Logger LOGGER = LogManager.getLogger();
    private final WhatsAppWebClient client;

    public CommandShowText(WhatsAppWebClient client) {
        super(COMMAND_SHOW_TEXT, "Shows the text of the textbox.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        try {
            Console.writeLine(client.getTextContent());
        } catch (TimeoutWhatsAppWebException | NoSuchWhatsAppWebElementException ex) {
            LOGGER.warn("Somtehing went wrong in getting content of chat textbox.");
            LOGGER.debug("Error",ex);
        }
        return Optional.empty();
    }

}
