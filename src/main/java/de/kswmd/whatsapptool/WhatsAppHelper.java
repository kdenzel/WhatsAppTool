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
import java.util.UUID;
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

    /**
     * Shift enter sequence if you want to put a new line to the chatbox inside
     * the same message block.
     */
    public final static String SHIFT_ENTER = Keys.chord(Keys.SHIFT, Keys.ENTER);
    /**
     * The emoji start sequence that is secret and will be used by the message
     * parser for replacing emojis found in the text and the whatsapp client has
     * to filter also for sending emojis because it depends on the popup dialog
     * in the whatsapp web frontend.
     *
     * See de.kswmd.whatsapptool.WhatsAppWebClient the appendText method for the
     * ugly emoji hack is used for.
     */
    public static final String EMOJI_START_SEQUENCE = UUID.randomUUID().toString();//"读写汉字";
    /**
     * The emoji end sequence that is secret and will be used by the message
     * parser for replacing emojis found in the text and the whatsapp client has
     * to filter also for sending emojis because it depends on the popup dialog
     * in the whatsapp web frontend.
     *
     * See de.kswmd.whatsapptool.WhatsAppWebClient the appendText method for the
     * ugly emoji hack is used for.
     */
    public static final String EMOJI_END_SEQUENCE = UUID.randomUUID().toString();//"读写汉字";

    /**
     * Max charcters for the textbox to be appended.
     */
    public static final int MAX_TEXTBOX_CHAR_SIZE = 65536;

    /**
     * Because the chromedriver only supports BMP Characters this is a solution
     * to send emojis to the whatsapp web frontend. It requires a char sequence
     * to select the specific emoji except for the enter. The enter key has to
     * be performed by the WhatsAppWebClient class itself in the appendText
     * Method. This is the reason for filtering emojis before being sent so the
     * popup dialog can appear.
     */
    public enum Emoji {
        ROFL(Keys.chord(":floor")),
        FACE_WITH_TEARS_OF_JOY(Keys.chord(":face with tears of joy")),
        CRAZY_FACE(Keys.chord(":crazy face")),
        SMILING_FACE_WITH_OPEN_MOUTH_AND_SMILING_EYES(Keys.chord(":smiling face with open", Keys.ARROW_RIGHT, Keys.ARROW_RIGHT)),
        SMILING_FACE_WITH_OPEN_MOUTH_AND_CLOSED_EYES(Keys.chord(":smiling face with open", Keys.RIGHT)),
        SMILING_FACE_WITH_HEART_EYES(Keys.chord(":smiling face with heart")),
        FACE_BLOWING_KISS(Keys.chord(":face blowing")),
        MIDDLE_FINGER(Keys.chord(":middle")),
        FACE_WITH_SYMBOLS_OVER_THE_MOUTH(Keys.chord(":face with symbols")),
        GRINNING_FACE(Keys.chord(":grinning face")),
        GRINNING_FACE_WITH_SMILING_EYES(Keys.chord(":grinning face with"));

        private final String sequence;

        Emoji(final String sequence) {
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
        Console.writeLine("Start sending Message process.");
        final long curserPosition = Console.writeAtEnd("");
        boolean notInContactList;
        try {
            ProgressBar.printProgress(startTime, total, 0, curserPosition);
            client.search(identifier, ChronoConstants.DURATION_OF_5_SECONDS);
            ProgressBar.printProgress(startTime, total, Math.round(5 * total / totalInSeconds), curserPosition);
            client.waitForTimeOut(ChronoConstants.DURATION_OF_1_SECOND);
            ProgressBar.printProgress(startTime, total, Math.round(6 * total / totalInSeconds), curserPosition);
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
        ProgressBar.printProgress(startTime, total, Math.round(8 * total / totalInSeconds), curserPosition);
        Console.writeLine();
        if (notInContactList) {
            if (!identifier.matches("^[+0-9]+")) {
                throw new NotAPhoneNumberException("The identifier '" + identifier + "' was neither found in your contacts nor is it a valid phone number.");
            }
            client.open(identifier);
            try {
                client.setText(content, ChronoConstants.DURATION_OF_10_SECONDS);
            } catch (TimeoutWhatsAppWebException ex) {
                LOGGER.trace("No Textbox found", ex);
                handlePossiblePopUpDialog(client);
            }
        } else {
            client.setText(content, ChronoConstants.DURATION_OF_10_SECONDS);
        }
        ProgressBar.printProgress(startTime, total, Math.round(18 * total / totalInSeconds), curserPosition);
        client.send(ChronoConstants.DURATION_OF_3_SECONDS);
        ProgressBar.printProgress(startTime, total, Math.round(21 * total / totalInSeconds), curserPosition);
        client.waitForTimeOut(ChronoConstants.DURATION_OF_500_MILLIS);
        ProgressBar.printProgress(startTime, total, total, curserPosition);
        Console.writeLine();
    }

    public static String makeBlocksIfNecessary(String textFieldContent, String origText) {
        int fullSize = textFieldContent.length() + origText.length();
        if (WhatsAppHelper.MAX_TEXTBOX_CHAR_SIZE <= fullSize) {
            StringBuilder sb = new StringBuilder(origText);
            int index = 0;
            int textFieldContentLength = textFieldContent.length();
            while (index < sb.length()) {
                int spaceLeftForBlock = Math.min(sb.length(), index + WhatsAppHelper.MAX_TEXTBOX_CHAR_SIZE - textFieldContentLength);
                sb.insert(Math.min(index + spaceLeftForBlock, sb.length()), '\n');
                index = spaceLeftForBlock + 1;
                //set textfield content to 0 because it is only relevant for the first Block.
                textFieldContentLength = 0;
            }
            return sb.toString();
        }
        return origText;
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
