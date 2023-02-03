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
import de.kswmd.whatsapptool.WhatsAppHelper;
import de.kswmd.whatsapptool.contacts.ChatListBean;
import static de.kswmd.whatsapptool.contacts.ChatListBean.Type.CONTACT;
import de.kswmd.whatsapptool.contacts.Message;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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

    public static final Logger LOGGER = LogManager.getLogger();

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_WHATSAPP_CLIENT = "whatsapp_client";
    private static final Duration WAIT_5_SECONDS = Duration.ofSeconds(5);
    private static final Duration WAIT_HALF_SECOND = Duration.ofMillis(500);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Message m = (Message) jec.getTrigger().getJobDataMap().get(KEY_MESSAGE);
        LOGGER.info(m.getEntity().getIdentifier() + ": " + m.getContent());
        WhatsAppClient client = (WhatsAppClient) jec.getJobDetail().getJobDataMap().get(KEY_WHATSAPP_CLIENT);
        StringBuilder errorMessage = new StringBuilder();
        if (client.search(m.getEntity().getIdentifier(), WAIT_5_SECONDS)) {
            client.waitForTimeOut(WAIT_HALF_SECOND);
            Optional<WebElement> chatList = client.getChatList();
            if (chatList.isPresent()) {
                List<ChatListBean> elements = WhatsAppHelper.generateFromWebElement(chatList.get()).stream().filter(cbl -> cbl.getType().equals(CONTACT)).collect(Collectors.toList());
                if (!elements.isEmpty()) {
                    ChatListBean clb = elements.get(0);
                    client.clickElement(By.xpath("//div[@data-testid='" + clb.getListItemTestId() + "']"));
                } else {
                    client.open(m.getEntity().getIdentifier());
                    errorMessage.append(StringUtils.trimToEmpty(getPopUpDialogContent(client)));
                }
            } else {
                client.open(m.getEntity().getIdentifier());
                errorMessage.append(StringUtils.trimToEmpty(getPopUpDialogContent(client)));
            }
            if (errorMessage.length() == 0) {
                try {
                    client.waitTilTextIsAvailable(2);
                    client.setText(m.getContent());
                    client.send(3);
                } catch (Exception ex) {
                    LOGGER.error("Exception occured...\n" + m, ex);
                }
            }
        }
        if (errorMessage.length() != 0) {
            LOGGER.error("Job execution failed... \n" + m + "\n" + errorMessage.toString());
        } else {
            LOGGER.info("Successfully sent message. " + m);
        }
    }

    private synchronized String getPopUpDialogContent(final WhatsAppClient client) {
        try {
            WebElement element = client.isPopUpPresent(WAIT_5_SECONDS);
            WebElement contents = element.findElement(By.xpath(".//div[@data-testid='popup-contents']"));
            WebElement button = element.findElement(By.xpath(".//div[@data-testid='popup-controls-ok']"));
            button.click();
            return contents.getText();
        } catch (Exception ex) {
            LOGGER.trace("Error in handling PopUp-Dialog...", ex);
        }
        return null;
    }

}
