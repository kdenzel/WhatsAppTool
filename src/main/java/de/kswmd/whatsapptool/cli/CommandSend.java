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

import de.kswmd.whatsapptool.WhatsAppClient;
import de.kswmd.whatsapptool.utils.ProgressBar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.openqa.selenium.Keys;

/**
 *
 * @author Kai Denzel
 */
public class CommandSend extends Command {

    private final WhatsAppClient client;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");

    public CommandSend(WhatsAppClient client) {
        super(COMMAND_SEND, "Sends the message that is displayed in the Textfield.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        if (parameters != null) {
            String p = (String) parameters;
            if (p.equals("withTimestamp")) {
                String text = client.getTextContent();
                LocalDateTime now = LocalDateTime.now();
                String date = now.format(formatter);
                client.setText(date + Keys.chord(Keys.SHIFT, Keys.ENTER) + text);
            }
        }
        int timeoutInSeconds = 3;
        ProgressBar progressBar = ProgressBar.getTimerBasedProgressBar(timeoutInSeconds, ChronoUnit.SECONDS);
        progressBar.start();
        boolean sended = client.send(timeoutInSeconds);
        progressBar.finish();
        if (sended) {
            Console.writeLine("The message was successfully sent to " + client.getConversationInfoHeader());
        } else {
            Console.writeLine("Couldn't send message. Be sure you opened a chat window and inserted some text and that you are authenticated.");
        }
        return Optional.empty();
    }

}
