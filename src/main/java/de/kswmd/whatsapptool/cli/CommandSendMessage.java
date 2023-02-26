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

import de.kswmd.whatsapptool.WhatsAppHelper;
import de.kswmd.whatsapptool.WhatsAppWebClient;
import de.kswmd.whatsapptool.text.MessageParser;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kai Denzel
 */
public class CommandSendMessage extends Command {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    private final WhatsAppWebClient client;
    
    public CommandSendMessage(final WhatsAppWebClient client) {
        super(COMMAND_SEND_MESSAGE, "Sends a message to the specified identifier. ${identifier} ${message}");
        this.client = client;
    }
    
    @Override
    public Optional<Object> execute(Object parameters) {
        String params = String.valueOf(parameters);
        int firstSpace = params.indexOf(' ', 0);
        if (firstSpace != -1) {
            String identifier = params.substring(0, firstSpace);
            try {
                String message = MessageParser.DEFAULT_PARSER.format(
                        params.substring(firstSpace + 1)
                ).replaceAll("\n", WhatsAppHelper.SHIFT_ENTER);
                WhatsAppHelper.sendMessage(identifier,
                        message,
                        client);
                LOGGER.info("Successfully sent message to " + identifier);
            } catch (Exception ex) {
                LOGGER.error("Couldn't send message.", ex);
            }
        } else {
            LOGGER.info("Missing identifier or empty message.");
        }
        return Optional.empty();
    }
    
}
