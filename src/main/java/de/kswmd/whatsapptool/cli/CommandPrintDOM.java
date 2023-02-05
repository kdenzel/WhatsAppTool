/*
 * The MIT License
 *
 * Copyright 2023 kdenzel.
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kdenzel
 */
public class CommandPrintDOM extends Command {

    private static final Logger LOGGER = LogManager.getLogger();

    private final WhatsAppWebClient client;

    public CommandPrintDOM(final WhatsAppWebClient client) {
        super(COMMAND_PRINT_DOM, "Prints the DOM to the console and if available to the specified file.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        String p = String.valueOf(parameters);
        String source = client.getDriver().getPageSource();
        if (!StringUtils.trimToEmpty(p).isEmpty()) {
            try {
                Files.write(Path.of(p), source.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                LOGGER.warn("Couldn't create file. " + ex.getMessage());
                LOGGER.debug("Error, couldn't create file...", ex);
            }
        }
        Console.writeLine(source);
        return Optional.empty();
    }

}
