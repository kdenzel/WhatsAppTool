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
package de.kswmd.whatsapptool.quartz;

import de.kswmd.whatsapptool.WhatsAppWebClient;
import de.kswmd.whatsapptool.WhatsAppHelper;
import de.kswmd.whatsapptool.contacts.Message;
import de.kswmd.whatsapptool.text.MessageParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Kai Denzel
 */
@DisallowConcurrentExecution
public class HandleCronMessageJob implements Job {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_WHATSAPP_CLIENT = "whatsapp_client";

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Message m = (Message) jec.getTrigger().getJobDataMap().get(KEY_MESSAGE);
        WhatsAppWebClient client = (WhatsAppWebClient) jec.getJobDetail().getJobDataMap().get(KEY_WHATSAPP_CLIENT);
        long ts = System.currentTimeMillis();
        try {
            LOGGER.info("Start sending message to " + m.getEntity().getIdentifier() + ": " + m.getContent());
            //TODO: Find a solution for linebreaks.
            m.setContent(MessageParser.DEFAULT_PARSER.format(m).replaceAll("\n", Keys.chord(Keys.SHIFT,Keys.ENTER)));
            WhatsAppHelper.sendMessage(m, client);
            LOGGER.info("Successfully sent message. " + (System.currentTimeMillis() - ts) + "ms:\n" + m);
        } catch (Exception ex) {
            LOGGER.error("Job execution failed. " + (System.currentTimeMillis() - ts) + "ms:\n" + m + "\n", ex);
        }
    }

}
