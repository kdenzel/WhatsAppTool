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

import de.kswmd.whatsapptool.PopUpDialogAvailableException;
import de.kswmd.whatsapptool.TimeoutWhatsAppWebException;
import de.kswmd.whatsapptool.WhatsAppWebClient;
import de.kswmd.whatsapptool.WhatsAppHelper;
import de.kswmd.whatsapptool.contacts.ChatListBean;
import static de.kswmd.whatsapptool.contacts.ChatListBean.Type.CONTACT;
import de.kswmd.whatsapptool.contacts.Message;
import de.kswmd.whatsapptool.utils.ChronoConstants;
import java.util.List;
import java.util.stream.Collectors;
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

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_WHATSAPP_CLIENT = "whatsapp_client";

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Message m = (Message) jec.getTrigger().getJobDataMap().get(KEY_MESSAGE);
        LOGGER.info("Start sending message to " + m.getEntity().getIdentifier() + ": " + m.getContent());
        WhatsAppWebClient client = (WhatsAppWebClient) jec.getJobDetail().getJobDataMap().get(KEY_WHATSAPP_CLIENT);

        try {
            client.search(m.getEntity().getIdentifier(), ChronoConstants.DURATION_OF_5_SECONDS);
            WebElement chatList = client.getChatList(ChronoConstants.DURATION_OF_500_MILLIS);
            List<ChatListBean> elements = WhatsAppHelper.generateFromWebElement(chatList).stream().filter(cbl -> cbl.getType().equals(CONTACT)).collect(Collectors.toList());
            if (!elements.isEmpty()) {
                ChatListBean clb = elements.get(0);
                client.clickElement(By.xpath("//div[@data-testid='" + clb.getListItemTestId() + "']"));
            } else {
                client.open(m.getEntity().getIdentifier());
                handlePossiblePopUpDialog(client);
            }

            client.setText(m.getContent(), ChronoConstants.DURATION_OF_10_SECONDS);
            client.send(ChronoConstants.DURATION_OF_3_SECONDS);
            LOGGER.info("Successfully sent message. " + m);
        } catch (TimeoutWhatsAppWebException | PopUpDialogAvailableException ex) {
            LOGGER.error("Job execution failed... \n" + m + "\n", ex);
        }
    }

    private synchronized void handlePossiblePopUpDialog(final WhatsAppWebClient client) throws PopUpDialogAvailableException {
        String content = null;
        WebElement element = null;
        try {
            element = client.getPopUp(ChronoConstants.DURATION_OF_5_SECONDS);
            WebElement contents = element.findElement(By.xpath(".//div[@data-testid='popup-contents']"));
            WebElement button = element.findElement(By.xpath(".//div[@data-testid='popup-controls-ok']"));
            button.click();
            content = contents.getText();
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Error in handling PopUp-Dialog...", ex);
        }
        if (element != null) {
            throw new PopUpDialogAvailableException(content);
        }
    }

}
