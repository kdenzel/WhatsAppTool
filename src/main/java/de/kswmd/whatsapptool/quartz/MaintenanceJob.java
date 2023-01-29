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

import de.kswmd.whatsapptool.WhatsAppClient;
import de.kswmd.whatsapptool.utils.Settings;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
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
public class MaintenanceJob implements Job {

    private final Logger LOGGER = LogManager.getLogger();

    final static String WEBDRIVER_FACTORY = "webdriverfactory";
    final static String WEBDRIVER = "webdriver";
    final static String WHATSAPP_CLIENT = "whatsappclient";

    public MaintenanceJob() {
        //LOGGER.info("Constructor called...");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            LocalDateTime now = LocalDateTime.now();
            String adminPhoneNumber = StringUtils.trimToNull(Settings.getInstance().getAdminPhoneNumber());
            if (adminPhoneNumber != null) {
                WhatsAppClient client = (WhatsAppClient) context.getJobDetail().getJobDataMap().get(WHATSAPP_CLIENT);
                client.open(adminPhoneNumber);
                client.waitTilTextIsAvailable(10);
                StringBuilder sb = new StringBuilder();
                sb.append(now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));
                String hostname = "unknown";
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException ex) {
                }
                sb.append(Keys.chord(Keys.SHIFT, Keys.ENTER));
                sb.append("Hostname: ");
                sb.append(hostname);
                sb.append(Keys.chord(Keys.SHIFT, Keys.ENTER));
                sb.append("Status OK :grinning face with ");
                sb.append(Keys.ENTER);
                client.setText(sb.toString());
                client.send(3);

            }
        } catch (Exception ex) {
            LOGGER.error("Fire Maintenance job failed...", ex);
        }
    }

}
