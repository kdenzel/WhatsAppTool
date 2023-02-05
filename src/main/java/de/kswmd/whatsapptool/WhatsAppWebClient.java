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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
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

    private final WebDriver driver;

    public WhatsAppWebClient(WebDriver driver) {
        this.driver = driver;
    }

    public synchronized void open() {
        driver.get("https://" + WHATSAPP_WEB_URI);
        acceptAlert();
    }

    public synchronized void open(String phone) {
        driver.get("https://" + WHATSAPP_WEB_URI + "/send?"
                + "phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8));
        acceptAlert();
    }

    public synchronized void openWithText(String phone, String text) {
        driver.get("https://" + WHATSAPP_WEB_URI + "/send?"
                + "phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8));
        acceptAlert();
    }

    public synchronized void refresh() {
        driver.navigate().refresh();
    }

    public synchronized WebElement getElement(By by, Duration timeout) throws TimeoutWhatsAppWebException {
        try {
            return new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (TimeoutException ex) {
            throw new TimeoutWhatsAppWebException("Element not available after timeout in seconds "
                    + String.format("%02d:%03d", timeout.toSeconds(), timeout.toMillisPart())
                    + " for by='" + by.toString() + "'...", ex);
        }
    }

    public synchronized List<WebElement> getElements(By by, Duration timeout) throws TimeoutWhatsAppWebException {
        try {
            return new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
        } catch (TimeoutException ex) {
            throw new TimeoutWhatsAppWebException("Elements not available after timeout in seconds "
                    + String.format("%02d:%03d", timeout.toSeconds(), timeout.toMillisPart())
                    + " for by='" + by.toString() + "'...", ex);
        }
    }

    public synchronized WebElement getConversationTextField(Duration timeout) throws TimeoutWhatsAppWebException {
        String xPath = "//div[@role='textbox'][@data-testid='conversation-compose-box-input']";
        return getElement(By.xpath(xPath), timeout);
    }

    public synchronized void appendText(String text, Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getConversationTextField(timeout);
        textField.sendKeys(text);
    }

    public synchronized void appendText(String text) throws TimeoutWhatsAppWebException {
        appendText(text, Duration.ZERO);
    }

    public synchronized void setText(String text) throws TimeoutWhatsAppWebException {
        setText(text, Duration.ZERO);
    }

    public synchronized void setText(String text, Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getConversationTextField(timeout);
        textField.sendKeys(Keys.CONTROL + "a");
        textField.sendKeys(Keys.DELETE);
        textField.sendKeys(text);
    }

    public synchronized void search(String text) throws TimeoutWhatsAppWebException {
        search(text, Duration.ZERO);
    }

    public synchronized void search(String text, Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getSearchTextBox(timeout);
        textField.sendKeys(Keys.CONTROL + "a");
        textField.sendKeys(Keys.DELETE);
        textField.sendKeys(text);
    }

    public synchronized WebElement getSearchTextBox(Duration timeout) throws TimeoutWhatsAppWebException {
        String xPath = "//div[@role='textbox'][@data-testid='chat-list-search']";
        return getElement(By.xpath(xPath), timeout);
    }

    public synchronized void clearConversationTextBox() throws TimeoutWhatsAppWebException {
        clearTextBox(Duration.ZERO);
    }

    public synchronized void clearTextBox(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement textField = getConversationTextField(timeout);
        textField.sendKeys(Keys.CONTROL + "a");
        textField.sendKeys(Keys.DELETE);
    }

    public synchronized String getTextContent() throws TimeoutWhatsAppWebException, NoSuchWhatsAppWebElementException {
        String xPathRelativeToConversationTextField = "./p/span";
        try {
            WebElement conversationTextBox = getConversationTextField(Duration.ZERO).findElement(By.xpath(xPathRelativeToConversationTextField));
            return conversationTextBox.getText();
        } catch (NoSuchElementException ex) {
            throw new NoSuchWhatsAppWebElementException("No element '" + xPathRelativeToConversationTextField + "' found in conversation text box.", ex);
        }
    }

    public synchronized String getConversationInfoHeaderText() throws TimeoutWhatsAppWebException {
        return getConversationInfoHeaderText(Duration.ZERO);
    }

    public synchronized String getConversationInfoHeaderText(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement text = getConversationInfoHeader(timeout);
        return text.getText();
    }

    public synchronized WebElement getConversationInfoHeader(Duration timeout) throws TimeoutWhatsAppWebException {
        String xPath = "//span[@data-testid='conversation-info-header-chat-title']";
        return getElement(By.xpath(xPath), timeout);
    }

    public synchronized WebElement getSendButton() throws TimeoutWhatsAppWebException {
        return getSendButton(Duration.ZERO);
    }

    public synchronized WebElement getSendButton(Duration timeout) throws TimeoutWhatsAppWebException {
        String xPath = "//button[@data-testid='compose-btn-send']";
        return getElement(By.xpath(xPath), timeout);
    }

    /**
     * clicks the send button with a timeout
     * @param timeout
     * @throws TimeoutWhatsAppWebException 
     */
    public synchronized void send(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement sendButton = getSendButton(timeout);
        LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, sendButton));
        sendButton.click();
    }

    public synchronized void clickElement(By by) throws TimeoutWhatsAppWebException {
        getElement(by, Duration.ZERO).click();
    }

    public synchronized void clickElement(By by, Duration timeout) throws TimeoutWhatsAppWebException {
        getElement(by, timeout).click();
    }

    public synchronized boolean isQRCodeVisible(Duration timeout) {
        try {
            WebElement qrCode = getElement(By.xpath("//canvas[@role='img']"), timeout);
            LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, qrCode));
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Couldn't find qrCode", ex);
        }
        return false;
    }

    public synchronized boolean isQRCodeVisible(long timeOutInSeconds) {
        return isQRCodeVisible(Duration.ofSeconds(timeOutInSeconds));
    }

    public synchronized boolean openChatWithUnreadNotification() {
        try {
            WebElement span = getElement(By.xpath("//span[@data-testid='icon-unread-count']"), Duration.ZERO);
            WebElement listItem = span.findElement(By.xpath("./ancestor::div[contains(@data-testid,'list-item-')]"));
            LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, listItem));
            listItem.click();
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Couldn't find a list element with icon-unread-count.", ex);
        }
        return false;
    }

    public synchronized List<WebElement> getSpansWithUnreadNotification() {
        try {
            return getElements(By.xpath("//span[@data-testid='icon-unread-count']"), Duration.ZERO);
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Couldn't find a list element with icon-unread-count.", ex);
        }
        return new ArrayList<>();
    }

    public synchronized WebElement getChatList(Duration timeout) throws TimeoutWhatsAppWebException {
        WebElement chatlist = getElement(By.xpath("//div[@data-testid='chat-list']"), timeout);
        return chatlist;
    }

    public synchronized WebElement getChatList() throws TimeoutWhatsAppWebException {
        return getChatList(Duration.ZERO);
    }

    public synchronized boolean checkAlertUpdate() {
        try {
            getAlertUpdate();
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Alert update not found", ex);
        }
        return false;
    }

    public synchronized boolean clickAlertUpdate() {
        try {
            WebElement alertUpdate = getAlertUpdate();
            alertUpdate.click();
            return true;
        } catch (TimeoutWhatsAppWebException ex) {
            LOGGER.trace("Alert update not found", ex);
        }
        return false;

    }

    public synchronized WebElement getAlertUpdate() throws TimeoutWhatsAppWebException {
        return getElement(By.xpath("//span[@data-testid='alert-update']"), Duration.ZERO);
    }

    public synchronized WebElement getStartUpProgressBar(long timeoutInMillis) throws TimeoutWhatsAppWebException {
        return getElement(By.xpath("//progress[not(@dir='ltr')]"), Duration.ofMillis(timeoutInMillis));
    }

    public synchronized WebElement getStartUpProgressBar(Duration timeout) throws TimeoutWhatsAppWebException {
        return getElement(By.xpath("//progress[not(@dir='ltr')]"), timeout);
    }

    public synchronized void waitForTimeOut(long seconds) {
        waitForTimeOut(Duration.ofSeconds(seconds));
    }

    public synchronized void waitForTimeOut(Duration duration) {
        try {
            new WebDriverWait(driver, duration).until(d -> {
                return false;
            });
        } catch (TimeoutException ex) {
            LOGGER.trace("Expected timeout", ex);
        }
    }

    public synchronized void waitForPageLoaded(Duration duration) throws TimeoutException {
        new WebDriverWait(driver, duration).until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    public synchronized String getUrl() {
        return driver.getCurrentUrl();
    }

    public synchronized WebDriver getDriver() {
        return driver;
    }

    public synchronized boolean isAlertPresent() {
        boolean foundAlert = false;
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

    public synchronized WebElement getPopUp(Duration duration) throws TimeoutWhatsAppWebException {
        return getElement(By.xpath("//div[@data-testid='confirm-popup']"), duration);
    }

    public synchronized void acceptAlert() {
        if (isAlertPresent()) {
            driver.switchTo().alert().accept();
        }
    }
}
