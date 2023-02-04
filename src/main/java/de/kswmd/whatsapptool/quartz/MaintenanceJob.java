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

import de.kswmd.whatsapptool.MiscConstants;
import de.kswmd.whatsapptool.WhatsAppClient;
import de.kswmd.whatsapptool.WhatsAppHelper.Emoji;
import de.kswmd.whatsapptool.utils.Settings;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    final static String WHATSAPP_CLIENT = "whatsappclient";
    final DateTimeFormatter DATE_TIME_FORMAT_LOCALE_DE = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
    final DateTimeFormatter DATE_FORMAT_YYYY_MM = DateTimeFormatter.ofPattern("yyyy-MM");
    final DateTimeFormatter DATE_FORMAT_MM_dd_YYYY = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public MaintenanceJob() {
        //LOGGER.info("Constructor called...");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Starting MaintenanceJob.");
        try {
            LocalDateTime now = LocalDateTime.now();
            String adminPhoneNumber = StringUtils.trimToNull(Settings.getInstance().getAdminPhoneNumber());
            if (adminPhoneNumber != null) {
                WhatsAppClient client = (WhatsAppClient) context.getJobDetail().getJobDataMap().get(WHATSAPP_CLIENT);
                client.open(adminPhoneNumber);
                client.waitTilTextIsAvailable(10);
                StringBuilder sb = new StringBuilder();
                sb.append(now.format(DATE_TIME_FORMAT_LOCALE_DE));
                String hostname = "unknown";
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException ex) {
                    LOGGER.trace("Hostname not found...", ex);
                }
                sb.append(Keys.chord(Keys.SHIFT, Keys.ENTER))
                        .append("Hostname: ")
                        .append(hostname)
                        .append(Keys.chord(Keys.SHIFT, Keys.ENTER))
                        .append("Status OK ")
                        .append(Emoji.GRINNING_FACE.getSequence());
                try {
                    LocalDateTime yesterday = now.minusDays(1);
                    String logFilePath = System.getProperty(MiscConstants.KEY_LOG_FILE_PATH);
                    String fileToRead = yesterday.format(DATE_FORMAT_YYYY_MM) + "/app-" + yesterday.format(DATE_FORMAT_MM_dd_YYYY) + ".message-job-error-log.txt";
                    String content = Files.readString(Paths.get(logFilePath + "/" + fileToRead));
                    content = content.replaceAll("\n", Keys.chord(Keys.SHIFT, Keys.ENTER));
                    sb.append(Keys.chord(Keys.SHIFT, Keys.ENTER));
                    sb.append(Keys.chord(Keys.SHIFT, Keys.ENTER));
                    if (!StringUtils.trimToEmpty(content).isEmpty()) {
                        sb.append(content);
                    } else {
                        sb.append("No errors logged.");
                    }
                } catch (IOException ex) {
                    LOGGER.error("Couldn't read message-job-error-log.txt...", ex);
                }
                client.setText(sb.toString());
                client.send(3);
            }
        } catch (Exception ex) {
            LOGGER.error("Fire Maintenance job failed...", ex);
        }
    }

}
