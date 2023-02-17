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
package de.kswmd.whatsapptool;

import de.kswmd.whatsapptool.cli.Console;
import de.kswmd.whatsapptool.contacts.ChatListBean;
import static de.kswmd.whatsapptool.contacts.ChatListBean.Type.CONTACT;
import de.kswmd.whatsapptool.contacts.Message;
import de.kswmd.whatsapptool.utils.ChronoConstants;
import de.kswmd.whatsapptool.utils.ProgressBar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Kai Denzel
 */
public class WhatsAppHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    public enum Emoji {
        MIDDLE_FINGER(":middle" + Keys.ENTER),
        GRINNING_FACE(":grinning face with " + Keys.ENTER);

        private final String sequence;

        Emoji(String sequence) {
            this.sequence = sequence;
        }

        public String getSequence() {
            return sequence;
        }
    }

    private WhatsAppHelper() {

    }

    public static String getAttributesOfElement(WebDriver driver, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        Object elementAttributes = executor.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", element);
        return elementAttributes.toString();
    }

    public static List<ChatListBean> generateFromWebElement(WebElement chatList) {
        List<WebElement> listItems = chatList.findElements(By.xpath(".//div[contains(@data-testid,'list-item-')]"));
        List<ChatListBean> list = new ArrayList<>(listItems.size());
        for (WebElement listItem : listItems) {
            try {
                ChatListBean bean = null;
                String sort = listItem.getAttribute("style");
                sort = sort.replaceAll("^.*translateY[(](.*)px[)].*$", "$1");
                //Check if list-item is a header element
                try {
                    WebElement header = listItem.findElement(By.xpath(".//div[@data-testid='section-header']"));
                    bean = new ChatListBean(ChatListBean.Type.HEADER);
                    bean.setTitle(header.getText());
                } catch (NoSuchElementException ex) {
                    LOGGER.trace("List Item has no header section...", ex);
                }
                //If it is null
                if (bean == null) {
                    String time = null;
                    try {
                        WebElement timeDiv = listItem.findElement(By.cssSelector("div.Dvjym"));
                        time = timeDiv.getText();
                    } catch (NoSuchElementException ex) {
                        LOGGER.trace("Element time div not found...", ex);
                    }
                    WebElement title = listItem.findElement(By.xpath(".//span[@dir='auto' and @title]"));
                    //Check if it is a message or a contact
                    try {
                        title.findElement(By.xpath("./ancestor::div[contains(@data-testid,'chatlist-message')]"));
                        bean = new ChatListBean(ChatListBean.Type.MESSAGE);
                    } catch (NoSuchElementException ex) {
                        LOGGER.trace("Not a message...", ex);
                        bean = new ChatListBean(ChatListBean.Type.CONTACT);
                    }
                    String lastMessageStatus = null;
                    try {
                        WebElement lastMessageStatusWebElement = listItem.findElement(By.xpath(".//span[@data-testid='last-msg-status']"));
                        lastMessageStatus = lastMessageStatusWebElement.getText().replaceAll("\n", "");
                    } catch (NoSuchElementException ex) {
                        LOGGER.trace("Element span last msg status not found...", ex);
                    }
                    int unreadMessages = 0;
                    try {
                        WebElement unreadCountSpan = listItem.findElement(By.xpath(".//span[@data-testid='icon-unread-count']"));
                        unreadMessages = Integer.parseInt(unreadCountSpan.getText());
                    } catch (NoSuchElementException ex) {
                        LOGGER.trace("Element span unread count not found...", ex);
                    } catch (NumberFormatException ex) {
                        LOGGER.trace("The text value of span element was not an integer...", ex);
                    }

                    bean.setTitle(title.getText());
                    bean.setTime(time);
                    bean.setLastMessage(lastMessageStatus);
                    bean.setUnreadMessages(unreadMessages);
                }
                bean.setListItemTestId(listItem.getAttribute("data-testid"));
                try {
                    bean.setSort(Integer.parseInt(sort));
                } catch (NumberFormatException ex) {
                    LOGGER.trace("Sort value invalid...", ex);
                }
                list.add(bean);
            } catch (Exception ex) {
                LOGGER.trace("Couldn't parse List-Item to Object...", ex);
            }
        }
//        if (listItems.size() < list.size()) {
//            list.subList(listItems.size() - 1, list.size() - 1).clear();
//        }

        Collections.sort(list);
        return list;
    }

    /**
     * Used for sending messages in concurrent situations like jobs with many
     * quartz triggers.It is a static synchronized method allowing only one
     * thread at a time sending messages
     *
     * @param m
     * @param client
     * @throws TimeoutWhatsAppWebException
     * @throws PopUpDialogAvailableException
     * @throws de.kswmd.whatsapptool.NotAPhoneNumberException
     */
    public static void sendMessage(final Message m, final WhatsAppWebClient client) throws TimeoutWhatsAppWebException, PopUpDialogAvailableException, NotAPhoneNumberException {
        sendMessage(m.getEntity().getIdentifier(), m.getContent(), client);
    }

    /**
     * Used for sending messages in concurrent situations like jobs with many
     * quartz triggers.It is a static synchronized method allowing only one
     * thread at a time sending messages
     *
     * @param identifier
     * @param content
     * @param client
     * @throws TimeoutWhatsAppWebException
     * @throws PopUpDialogAvailableException
     * @throws de.kswmd.whatsapptool.NotAPhoneNumberException
     */
    public static synchronized void sendMessage(final String identifier, final String content, final WhatsAppWebClient client) throws TimeoutWhatsAppWebException, PopUpDialogAvailableException, NotAPhoneNumberException {
        long startTime = System.currentTimeMillis();
        final float totalInSeconds = 22;
        final long total = 100;
        long curserPosition = Console.writeAtEnd("");
        Console.writeLine();
        ProgressBar.printProgress(startTime, total, 0, curserPosition);
        client.search(identifier, ChronoConstants.DURATION_OF_5_SECONDS);
        ProgressBar.printProgress(startTime, total, Math.round(5 * total / totalInSeconds), curserPosition);
        client.waitForTimeOut(ChronoConstants.DURATION_OF_1_SECOND);
        ProgressBar.printProgress(startTime, total, Math.round(6 * total / totalInSeconds), curserPosition);
        boolean notInContactList;
        try {
            WebElement chatList = client.getChatList(ChronoConstants.DURATION_OF_1_SECOND);
            ProgressBar.printProgress(startTime, total, Math.round(7 * total / totalInSeconds), curserPosition);
            Optional<ChatListBean> optionalChatListBean = WhatsAppHelper
                    .generateFromWebElement(chatList)
                    .stream()
                    .filter(cbl -> cbl.getType().equals(CONTACT))
                    .findFirst();
            notInContactList = optionalChatListBean.isEmpty();
            if (optionalChatListBean.isPresent()) {
                ChatListBean clb = optionalChatListBean.get();
                if (clb.getTitle().equals(identifier)) {
                    client.clickElement(By.xpath("//span[@dir='auto' and @title='"
                            + clb.getTitle()
                            + "']/ancestor::div[contains(@data-testid,'list-item')]"),
                            ChronoConstants.DURATION_OF_1_SECOND
                    );
                } else {
                    notInContactList = true;
                }
            }
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Error", ex);
            notInContactList = true;
        }

        if (notInContactList) {
            if (!identifier.matches("^[+0-9]+")) {
                throw new NotAPhoneNumberException("The identifier '" + identifier + "' was neither found in your contacts nor is it a valid phone number.");
            }
            client.open(identifier);
            try {
                ProgressBar.printProgress(startTime, total, Math.round(8 * total / totalInSeconds), curserPosition);
                client.setText(content, ChronoConstants.DURATION_OF_10_SECONDS);
            } catch (TimeoutWhatsAppWebException ex) {
                LOGGER.trace("No Textbox found", ex);
                handlePossiblePopUpDialog(client);
            }
        } else {
            ProgressBar.printProgress(startTime, total, Math.round(8 * total / totalInSeconds), curserPosition);
            client.setText(content, ChronoConstants.DURATION_OF_10_SECONDS);
        }
        ProgressBar.printProgress(startTime, total, Math.round(18 * total / totalInSeconds), curserPosition);
        client.send(ChronoConstants.DURATION_OF_3_SECONDS);
        ProgressBar.printProgress(startTime, total, Math.round(21 * total / totalInSeconds), curserPosition);
        client.waitForTimeOut(ChronoConstants.DURATION_OF_500_MILLIS);
        ProgressBar.printProgress(startTime, total, total, curserPosition);
    }

    private static void handlePossiblePopUpDialog(final WhatsAppWebClient client) throws PopUpDialogAvailableException {
        String content = null;
        WebElement element = null;
        try {
            element = client.getPopUp(ChronoConstants.DURATION_OF_2_SECONDS);
            WebElement contents = client.getElement(By.xpath("//div[@data-testid='confirm-popup']//div[@data-testid='popup-contents']"), ChronoConstants.DURATION_OF_2_SECONDS);
            WebElement button = client.getElement(By.xpath("//div[@data-testid='confirm-popup']//div[@data-testid='popup-controls-ok']"), ChronoConstants.DURATION_OF_2_SECONDS);
            button.click();
            content = contents.getText();
            client.waitForTimeOut(ChronoConstants.DURATION_OF_500_MILLIS);
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Error in handling PopUp-Dialog...", ex);
        }
        if (element != null) {
            throw new PopUpDialogAvailableException(content);
        }
    }

}
