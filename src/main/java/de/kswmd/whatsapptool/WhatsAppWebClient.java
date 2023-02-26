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

import static de.kswmd.whatsapptool.WhatsAppHelper.EMOJI_END_SEQUENCE;
import static de.kswmd.whatsapptool.WhatsAppHelper.EMOJI_START_SEQUENCE;
import de.kswmd.whatsapptool.WhatsAppHelper.Emoji;
import de.kswmd.whatsapptool.cli.Console;
import de.kswmd.whatsapptool.utils.ProgressBar;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Kai Denzel
 */
public class WhatsAppWebClient {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String WHATSAPP_WEB_URI = "web.whatsapp.com";

    public static final By XPATH_DIV_SEARCH_TEXTBOX = By.xpath("//div[@role='textbox'][@data-testid='chat-list-search']");
    public static final By XPATH_SPAN_CONVERSATION_INFO_HEADER = By.xpath("//span[@data-testid='conversation-info-header-chat-title']");
    public static final By XPATH_BUTTON_SEND = By.xpath("//button[@data-testid='compose-btn-send']");
    public static final By XPATH_CANVAS_QR_CODE_ELEMENT = By.xpath("//canvas[@role='img']");
    public static final By XPATH_DIV_CHAT_LIST = By.xpath("//div[@data-testid='chat-list']");
    public static final By XPATH_SPAN_ALERT_UPDATE = By.xpath("//span[@data-testid='alert-update']");
    public static final By XPATH_PROGRESS_STARTUP = By.xpath("//progress[not(@dir='ltr')]");
    public static final By XPATH_DIV_CONFIRM_POPUP = By.xpath("//div[@data-testid='confirm-popup']");
    public static final By XPATH_SPAN_UNDREAD_COUNT = By.xpath("//span[@data-testid='icon-unread-count']");
    public static final By XPATH_DIV_CHAT_TEXTBOX = By.xpath("//div[@role='textbox'][@data-testid='conversation-compose-box-input']");
    public static final By XPATH_ANCESTOR_DIV_LIST_ITEM = By.xpath("./ancestor::div[contains(@data-testid,'list-item-')]");
    public static final By XPATH_LI_EMOJI_VARIANT = By.xpath("//li[@data-testid='mi-emoji-variant']");

    private static final By XPATH_RELATIVE_SPAN_DIV_CHAT_TEXTBOX_TEXT = By.xpath("./p/span");

    private final WebDriver driver;

    /**
     * The secret pattern to filter out emojis from the message. Should be
     * impossible to guess by the user so the user has to go the way with the
     * message parser.
     */
    private final Pattern emojiPattern = Pattern.compile(EMOJI_START_SEQUENCE + "([A-Z_]+?)" + EMOJI_END_SEQUENCE);
    /**
     * Looks if the String ends with a word character.
     */
    private final Pattern stringEndingPattern = Pattern.compile(".*[\\w]$", Pattern.DOTALL);

    public WhatsAppWebClient(WebDriver driver) {
        this.driver = driver;
    }

    public void open() {
        driver.get("https://" + WHATSAPP_WEB_URI);
        acceptAlert();
    }

    public void open(String phone) {
        driver.get("https://" + WHATSAPP_WEB_URI + "/send?"
                + "phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8));
        acceptAlert();
    }

    public void openWithText(String phone, String text) {
        driver.get("https://" + WHATSAPP_WEB_URI + "/send?"
                + "phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8));
        acceptAlert();
    }

    public void refresh() {
        driver.navigate().refresh();
    }

    public WebElement getElement(By by, Duration timeout) throws TimeoutWhatsAppWebException {
        try {
            return new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (TimeoutException ex) {
            throw new TimeoutWhatsAppWebException("Element not available after timeout in seconds "
                    + String.format("%02d:%03d", timeout.toSeconds(), timeout.toMillisPart())
                    + " for by='" + by.toString() + "'...", ex);
        }
    }

    public List<WebElement> getElements(By by, Duration timeout) throws TimeoutWhatsAppWebException {
        try {
            return new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
        } catch (TimeoutException ex) {
            throw new TimeoutWhatsAppWebException("Elements not available after timeout in seconds "
                    + String.format("%02d:%03d", timeout.toSeconds(), timeout.toMillisPart())
                    + " for by='" + by.toString() + "'...", ex);
        }
    }

    public WebElement getConversationTextField(Duration timeout) throws TimeoutWhatsAppWebException {
        return getElement(XPATH_DIV_CHAT_TEXTBOX, timeout);
    }

    /**
     * This method is used for appending Text to the conversation text box if
     * shown. It also filters the Text for inserted Emojis performed by the
     * MessageParser class.
     *
     * @param text
     * @param timeout
     * @throws TimeoutWhatsAppWebException
     */
    public void appendText(String text, Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getConversationTextField(timeout);
        //Replace every Tab because Tab will lose the focus for the textbox.
        text = text.replaceAll("\t", "");
        final long curserPosition = Console.writeLine("Start appending Text.");
        final long startTime = System.currentTimeMillis();
        final int total = text.length();
        int progress = 0;
        //##########start ugly hack for sending emojis###########################
        int index = 0;
        Matcher matcher = emojiPattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            //Get the String before the next emoji
            String sub = text.substring(index, start);
            //Send them in junks
            sendInJunks(sub, textField, startTime, curserPosition, total, progress);
            //If the string ends with a word character, we have to insert a space.
            if (stringEndingPattern.matcher(sub).matches()) {
                textField.sendKeys(Keys.SPACE);
            }
            //Get the emoji
            String emojiValue = matcher.group(1);
            try {
                //Load the emoji
                Emoji emoji = Emoji.valueOf(emojiValue);
                //Calculate the free space from the chatbox
                int textFieldSize = textField.getText().length();
                int freeSpaceInCurrentBlock = WhatsAppHelper.MAX_TEXTBOX_CHAR_SIZE - textFieldSize;
                //if there isn't enough space for the emoji sequence press enter
                if (freeSpaceInCurrentBlock < emoji.getSequence().length()) {
                    textField.sendKeys(Keys.ENTER);
                }
                //print the emoji sequence
                textField.sendKeys(emoji.getSequence());
                //wait 10 millis before sending enter.
                long ts = System.currentTimeMillis() + 10;
                while (ts < System.currentTimeMillis());
                textField.sendKeys(Keys.ENTER);
                //select first available variant of emoji if it pops up.
                try {
                    WebElement firstEmojiVariant = getElement(XPATH_LI_EMOJI_VARIANT, Duration.ZERO);
                    firstEmojiVariant.click();
                } catch (TimeoutWhatsAppWebException ex) {
                    LOGGER.trace("No emoji variant popup found for emoji.");
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.fatal("How is this even possible?"
                        + " This should never be shown to the user because the match sequence is unknown."
                        + " Invalid emoji " + emojiValue, ex);
                textField.sendKeys(ex.getMessage());
            }
            index = matcher.end();
            progress = index;
            ProgressBar.printProgress(startTime, total, progress, curserPosition);
        }
        //##########end ugly hack for sending emojis###########################
        //Send the rest of the string or if no emoji available send the text.
        sendInJunks(text.substring(index, text.length()),
                textField, startTime, curserPosition, total, progress);
        Console.writeLine("");
    }

    /**
     * Method for buffering the text to the chatbox and sends the buffer when
     * the textbox reaches its limit.
     *
     * @param text
     * @param textField
     * @param startTime
     * @param curserPosition
     * @param total
     * @param progress
     */
    private void sendInJunks(String text, WebElement textField, long startTime, long curserPosition, int total, int progress) {
        final int junkBufferSize = 1024;
        int index = 0;
        while (index < text.length()) {
            int textFieldSize = textField.getText().length();
            int freeSpaceInCurrentBlock = Math.max(0, WhatsAppHelper.MAX_TEXTBOX_CHAR_SIZE - textFieldSize);
            int maxJunkSize = Math.min(freeSpaceInCurrentBlock, junkBufferSize);
            int endIndex = Math.min(index + maxJunkSize, text.length());
            String junk = text.substring(index, endIndex);
            int junkLength = junk.length();
            index = index + junkLength;
            if (freeSpaceInCurrentBlock == 0) {
                textField.sendKeys(Keys.ENTER);
            }
            textField.sendKeys(junk);
            progress += junkLength;
            ProgressBar.printProgress(startTime, total, progress, curserPosition);
        }

    }

    public void appendText(String text) throws TimeoutWhatsAppWebException {
        appendText(text, Duration.ZERO);
    }

    public void setText(String text) throws TimeoutWhatsAppWebException {
        setText(text, Duration.ZERO);
    }

    public void setText(String text, Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getConversationTextField(timeout);
        textField.sendKeys(Keys.CONTROL + "a");
        textField.sendKeys(Keys.DELETE);
        appendText(text);
    }

    public void search(String text) throws TimeoutWhatsAppWebException {
        search(text, Duration.ZERO);
    }

    public void search(String text, Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getSearchTextBox(timeout);
        textField.sendKeys(Keys.CONTROL + "a");
        textField.sendKeys(Keys.DELETE);
        textField.sendKeys(text);
    }

    public WebElement getSearchTextBox(Duration timeout) throws TimeoutWhatsAppWebException {
        return getElement(XPATH_DIV_SEARCH_TEXTBOX, timeout);
    }

    public void clearConversationTextBox() throws TimeoutWhatsAppWebException {
        clearTextBox(Duration.ZERO);
    }

    public void clearTextBox(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getConversationTextField(timeout);
        textField.sendKeys(Keys.CONTROL + "a");
        textField.sendKeys(Keys.DELETE);
    }

    public String getTextContent() throws TimeoutWhatsAppWebException, NoSuchWhatsAppWebElementException {
        try {
            WebElement conversationTextBox = getConversationTextField(Duration.ZERO).findElement(XPATH_RELATIVE_SPAN_DIV_CHAT_TEXTBOX_TEXT);
            return conversationTextBox.getText();
        } catch (NoSuchElementException ex) {
            throw new NoSuchWhatsAppWebElementException("No element '" + XPATH_RELATIVE_SPAN_DIV_CHAT_TEXTBOX_TEXT + "' found in conversation text box.", ex);
        }
    }

    public String getConversationInfoHeaderText() throws TimeoutWhatsAppWebException {
        return getConversationInfoHeaderText(Duration.ZERO);
    }

    public String getConversationInfoHeaderText(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement text = getConversationInfoHeader(timeout);
        return text.getText();
    }

    public WebElement getConversationInfoHeader(Duration timeout) throws TimeoutWhatsAppWebException {
        return getElement(XPATH_SPAN_CONVERSATION_INFO_HEADER, timeout);
    }

    public WebElement getSendButton() throws TimeoutWhatsAppWebException {
        return getSendButton(Duration.ZERO);
    }

    public WebElement getSendButton(Duration timeout) throws TimeoutWhatsAppWebException {
        return getElement(XPATH_BUTTON_SEND, timeout);
    }

    /**
     * clicks the send button with a timeout
     *
     * @param timeout
     * @throws TimeoutWhatsAppWebException
     */
    public void send(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement sendButton = getSendButton(timeout);
        LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, sendButton));
        sendButton.click();
    }

    public void clickElement(By by) throws TimeoutWhatsAppWebException {
        getElement(by, Duration.ZERO).click();
    }

    public void clickElement(By by, Duration timeout) throws TimeoutWhatsAppWebException {
        getElement(by, timeout).click();
    }

    public boolean isQRCodeVisible(Duration timeout) {
        try {
            WebElement qrCode = getElement(XPATH_CANVAS_QR_CODE_ELEMENT, timeout);
            LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, qrCode));
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Couldn't find qrCode", ex);
        }
        return false;
    }

    public boolean isQRCodeVisible(long timeOutInSeconds) {
        return isQRCodeVisible(Duration.ofSeconds(timeOutInSeconds));
    }

    public boolean openChatWithUnreadNotification() {
        try {
            WebElement span = getElement(XPATH_SPAN_UNDREAD_COUNT, Duration.ZERO);
            WebElement listItem = span.findElement(XPATH_ANCESTOR_DIV_LIST_ITEM);
            LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, listItem));
            listItem.click();
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Couldn't find a list element with icon-unread-count.", ex);
        }
        return false;
    }

    public List<WebElement> getSpansWithUnreadNotification() {
        try {
            return getElements(XPATH_SPAN_UNDREAD_COUNT, Duration.ZERO);
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Couldn't find a list element with icon-unread-count.", ex);
        }
        return new ArrayList<>();
    }

    public WebElement getChatList(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement chatlist = getElement(XPATH_DIV_CHAT_LIST, timeout);
        return chatlist;
    }

    public WebElement getChatList() throws TimeoutWhatsAppWebException {
        return getChatList(Duration.ZERO);
    }

    public boolean checkAlertUpdate() {
        try {
            getAlertUpdate();
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Alert update not found", ex);
        }
        return false;
    }

    public boolean clickAlertUpdate() {
        try {
            WebElement alertUpdate = getAlertUpdate();
            alertUpdate.click();
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Alert update not found", ex);
        }
        return false;

    }

    public WebElement getAlertUpdate() throws TimeoutWhatsAppWebException {
        return getElement(XPATH_SPAN_ALERT_UPDATE, Duration.ZERO);
    }

    public WebElement getStartUpProgressBar(long timeoutInMillis) throws TimeoutWhatsAppWebException {
        return getElement(XPATH_PROGRESS_STARTUP, Duration.ofMillis(timeoutInMillis));
    }

    public WebElement getStartUpProgressBar(Duration timeout) throws TimeoutWhatsAppWebException {
        return getElement(XPATH_PROGRESS_STARTUP, timeout);
    }

    public void waitForTimeOut(long seconds) {
        waitForTimeOut(Duration.ofSeconds(seconds));
    }

    public void waitForTimeOut(Duration duration) {
        try {
            new WebDriverWait(driver, duration).until(d -> {
                return false;
            });
        } catch (TimeoutException ex) {
            LOGGER.trace("Expected timeout", ex);
        }
    }

    public void waitForPageLoaded(Duration duration) throws TimeoutException {
        new WebDriverWait(driver, duration).until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    public String getUrl() {
        return driver.getCurrentUrl();
    }

    public WebDriver getDriver() {
        return driver;
    }

    public boolean isAlertPresent() {
        boolean foundAlert;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ZERO);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            foundAlert = true;
        } catch (TimeoutException eTO) {
            LOGGER.trace("No alert found...", eTO);
            foundAlert = false;
        }
        return foundAlert;
    }

    public WebElement getPopUp(Duration duration) throws TimeoutWhatsAppWebException {
        return getElement(XPATH_DIV_CONFIRM_POPUP, duration);
    }

    public void acceptAlert() {
        if (isAlertPresent()) {
            driver.switchTo().alert().accept();
        }
    }
}
