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
import de.kswmd.whatsapptool.contacts.MessageDatabase;
import de.kswmd.whatsapptool.quartz.ScheduleManager;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Kai Denzel
 */
public class CommandReloadNotifications extends Command {

    private static final Logger LOGGER = LogManager.getLogger();
    private final MessageDatabase messageDatabase;
    private final WhatsAppClient client;

    public CommandReloadNotifications(final MessageDatabase messageDatabase, final WhatsAppClient client) {
        super(COMMAND_RELOAD_NOTIFICATIONS_JOB, "Reloads the notifications.xml with all its Triggers.");
        this.messageDatabase = messageDatabase;
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        ScheduleManager manager = ScheduleManager.getInstance();
        try {
            messageDatabase.loadEntities();
            manager.scheduleMessagesJob(messageDatabase.getEntities(), client);
            manager.resumeAllJobs();
            Console.writeLine("Successfully scheduled the job.");
        }
        catch (Exception ex) {
            LOGGER.error("Couldn't create CronJob for notifications.", ex);
        }
        return Optional.empty();
    }

}
