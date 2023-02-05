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
import de.kswmd.whatsapptool.utils.ChronoConstants;
import de.kswmd.whatsapptool.utils.ProgressBar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;

/**
 *
 * @author Kai Denzel
 */
public class CommandSend extends Command {

    private static final Logger LOGGER = LogManager.getLogger();

    private final WhatsAppWebClient client;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");

    public CommandSend(WhatsAppWebClient client) {
        super(COMMAND_SEND, "Sends the message that is displayed in the Textfield.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        if (parameters != null) {
            String p = (String) parameters;
            if (p.equals("withTimestamp")) {
                try {
                    String text = client.getTextContent();
                    LocalDateTime now = LocalDateTime.now();
                    String date = now.format(formatter);
                    client.setText(date + Keys.chord(Keys.SHIFT, Keys.ENTER) + text);
                } catch (TimeoutWhatsAppWebException | NoSuchWhatsAppWebElementException ex) {
                    LOGGER.warn("Something went wrong in setting the timestamp for the message. " + ex.getMessage());
                    LOGGER.debug("Error...",ex);
                }
            }
        }
        ProgressBar progressBar = ProgressBar.getTimerBasedProgressBar(ChronoConstants.DURATION_OF_3_SECONDS, ChronoUnit.SECONDS);
        progressBar.start();
        try {
            client.send(ChronoConstants.DURATION_OF_3_SECONDS);
            Console.writeLine("The message was successfully sent to " + client.getConversationInfoHeaderText());
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.warn("Something went wrong in sending message. " + ex.getMessage());
            LOGGER.debug("Error...",ex);
        } finally {
            progressBar.finish();
        }
        return Optional.empty();
    }

}
