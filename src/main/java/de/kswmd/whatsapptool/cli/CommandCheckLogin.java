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
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kai Denzel
 */
public class CommandCheckLogin extends Command {

    private final Logger LOGGER = LogManager.getLogger();

    private final WhatsAppClient client;
    private final int limitInSeconds = 10;

    public CommandCheckLogin(WhatsAppClient client) {
        super(COMMAND_CHECK_LOGGEDIN, "Scans the website for the qr Code. If visible, you have to login again.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        try {
            Integer timeoutInSeconds = Integer.valueOf(parameters.toString());
            if (timeoutInSeconds < 0 || timeoutInSeconds > limitInSeconds) {
                throw new Exception("Timeout in Seconds is negative or bigger than " + limitInSeconds);
            }
            LOGGER.info("Check for QRCode with timeout of " + timeoutInSeconds + " seconds.");
            ProgressBar progressBar = ProgressBar.getTimerBasedProgressBar(timeoutInSeconds, ChronoUnit.SECONDS);
            boolean qrCodeVisible = client.isQRCodeVisible(timeoutInSeconds);
            progressBar.finish();
            if (qrCodeVisible) {
                Console.writeLine("You have to scan the QR-Code again.");
            } else {
                Console.writeLine("QR-Code isn't visible. Try to open a chat window and send a message.");
            }
            return Optional.of(!qrCodeVisible);
        } catch (Exception ex) {
            Console.writeLine("Invalid Timeout Value. It must be an integer value >= 0 and <= " + limitInSeconds);
            LOGGER.trace("Error", ex);
        }
        return Optional.empty();
    }

}
