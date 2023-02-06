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

import de.kswmd.whatsapptool.TimeoutWhatsAppWebException;
import de.kswmd.whatsapptool.WhatsAppWebClient;
import de.kswmd.whatsapptool.contacts.MessageFileDatabase;
import de.kswmd.whatsapptool.utils.ChronoConstants;
import de.kswmd.whatsapptool.utils.ProgressBar;
import de.kswmd.whatsapptool.utils.Settings;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.UserInterruptException;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

/**
 *
 * @author Kai Denzel
 */
public class CLI {

    /**
     * The Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * WhatsApp-Client to perform operations on WhatsApp-Web via the WebDriver.
     */
    private final WhatsAppWebClient whatsAppClient;
    /**
     * Flag to set Application running.
     */
    private boolean running;
    /**
     * Set of commands.
     */
    private final Set<Command> commands = new TreeSet<>();

    /**
     * The CLI-Constructor.
     *
     * @param client
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public CLI(final WhatsAppWebClient client) throws SAXException, IOException, ParserConfigurationException {
        this.whatsAppClient = client;
        commands.add(new CommandExit(this));
        commands.add(new CommandHelp(commands));
        commands.add(new CommandSend(client));
        commands.add(new CommandOpen(client));
        commands.add(new CommandSetText(client));
        commands.add(new CommandShowText(client));
        commands.add(new CommandCheckLogin(client));
        commands.add(new CommandPauseJob());
        commands.add(new CommandResumeJob());
        commands.add(new CommandShowInfoHeader(client));
        commands.add(new CommandClickNotification(client));
        commands.add(new CommandShowNotifications(client));
        commands.add(new CommandPrintChatList(client));
        commands.add(new CommandCheckForUpdate(client));
        commands.add(new CommandUpdate(client));
        commands.add(new CommandClear());
        commands.add(new CommandRefresh(client));
        commands.add(new CommandPrintDOM(client));
        commands.add(new CommandReloadNotifications(MessageFileDatabase.create(Settings.getInstance().getNotificationsXMLFile()), client));
        commands.add(new CommandSearchContacts(client));
        Console.initLineReader(
                commands
                        .stream()
                        .map(c -> c.getCommand()).toArray(String[]::new)
        );
    }

    /**
     * Starting the CLI.
     */
    public void start() {
        whatsAppClient.open();
        ProgressBar progressBar = null;
        try {
            progressBar = ProgressBar.getTimerBasedProgressBar(
                    ChronoConstants.DURATION_OF_5_SECONDS,
                    ChronoUnit.SECONDS
            );
            LOGGER.info("Wait for Browser to load Website. Timeout = "
                    + ChronoConstants.DURATION_OF_5_SECONDS.getSeconds()
                    + " seconds.");
            progressBar.start();
            WebElement progress = whatsAppClient.getStartUpProgressBar(
                    ChronoConstants.DURATION_OF_5_SECONDS
            );
            progressBar.finish();
            LOGGER.info("Website was loaded successfully. WhatsApp Loading Screen is showing.");
            int value = Integer.parseInt(progress.getAttribute("value"));
            int lastValue = -1;
            final int max = Integer.parseInt(progress.getAttribute("max"));

            progressBar = ProgressBar.getTimerBasedProgressBar(ChronoConstants.DURATION_OF_30_SECONDS, ChronoUnit.SECONDS);
            progressBar.start(Console.LINE_BREAK + "Time for timeout in seconds" + Console.LINE_BREAK);
            final long lineNumber = Console.write(Console.LINE_BREAK + "Progressbar" + Console.LINE_BREAK);
            long startTime = System.currentTimeMillis();
            long currentTime = startTime;
            long endTime = System.currentTimeMillis() + ChronoConstants.DURATION_OF_30_SECONDS.toMillis();
            while (value < max && currentTime < endTime) {
                if (lastValue != value) {
                    ProgressBar.printProgress(startTime, max, value, lineNumber);
                }
                value = Integer.parseInt(progress.getAttribute("value"));
                lastValue = value;
                currentTime = System.currentTimeMillis();
            }
            ProgressBar.printProgress(startTime, max, value, lineNumber);
            Console.writeLine();
            progressBar.finish();
            LOGGER.info("Website was loaded successfully. You are now able to send messages to your contacts.");
            running = true;
        } catch (TimeoutWhatsAppWebException ex) {
            if (progressBar != null && !progressBar.isFinished()) {
                progressBar.finish();
            }
            LOGGER.debug("Error", ex);
        } catch (NumberFormatException ex) {
            LOGGER.debug("This is unexpected...", ex);
        }
        
        if (!running) {
            final long timeoutInSeconds = ChronoConstants.DURATION_OF_5_SECONDS.toSeconds();
            LOGGER.info("Couldn't load Chats and Contacts. Check if QR-Code is visible. Timeout = " + timeoutInSeconds);
            isLoggedIn(timeoutInSeconds);
        }
        running = true;
        while (running) {
            String line;
            try {
                line = Console.readLine();
            } catch (UserInterruptException ex) {
                LOGGER.trace("User pressed CTRL+C", ex);
                line = Command.COMMAND_EXIT;
            }
            int firstSpace = line.indexOf(' ');
            String command = firstSpace != -1 ? line.substring(0, firstSpace).trim() : line.trim();
            String parameters = line.substring(command.length()).trim();

            Optional<Command> optionalC = getCommand(command.trim().toLowerCase());
            if (optionalC.isPresent()) {
                optionalC.get().execute(parameters);
            } else {
                Console.writeLine("No command found.");
            }
        }
    }

    /**
     * Stopping the CLI.
     */
    public void stop() {
        running = false;
    }

    /**
     * Checks with timeout if the user is registered in WhatsApp-Web. This is
     * done by looking if the QR-Code is visible or not.
     *
     * @param timeoutInSeconds
     * @return
     */
    public boolean isLoggedIn(long timeoutInSeconds) {
        Optional<Object> result = getCommand(Command.COMMAND_CHECK_LOGGEDIN).get().execute(timeoutInSeconds);
        return result.isPresent() && (boolean) result.get();
    }

    /**
     * Gives the command object back matching c.
     *
     * @param c the command as raw String
     * @return the Command Object
     */
    private Optional<Command> getCommand(String c) {
        return commands.stream().filter(command -> c != null && command.getCommand().equals(c)).findFirst();
    }

}
