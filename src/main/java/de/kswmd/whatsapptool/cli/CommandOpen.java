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

import de.kswmd.whatsapptool.WhatsAppWebClient;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Kai Denzel
 */
public class CommandOpen extends Command {

    private final WhatsAppWebClient client;

    public CommandOpen(WhatsAppWebClient client) {
        super(COMMAND_OPEN, "Opens the chat window with the specified Phonenumber. The Number must start with the country code.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        String p = (String) parameters;
        if (!StringUtils.trimToEmpty(p).isEmpty()) {
            client.open(p);
        } else {
            Console.writeLine("Please enter a Phonenumber as Parameter.");
        }
        Console.writeLine("Opened URL " + client.getUrl());
        return Optional.empty();
    }

}
