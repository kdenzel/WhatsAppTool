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
public class WhatsAppClient {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String WHATSAPP_WEB_URI = "web.whatsapp.com";

    private final WebDriver driver;

    public WhatsAppClient(WebDriver driver) {
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

    public synchronized boolean waitTilTextIsAvailable(Duration duration) {
        try {
            new WebDriverWait(driver, duration)
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@role='textbox'][@data-testid='conversation-compose-box-input']")));
            return true;
        } catch (Exception ex) {
            LOGGER.trace("Couldn't find Textbox.", ex);
        }
        return false;
    }

    public synchronized boolean waitTilTextIsAvailable(long seconds) {
        return waitTilTextIsAvailable(Duration.ofSeconds(seconds));
    }

    public synchronized void appendText(String text) {
        try {
            WebElement textField = driver.findElement(By.xpath("//div[@role='textbox'][@data-testid='conversation-compose-box-input']"));
            textField.sendKeys(text);
        } catch (NoSuchElementException ex) {
            LOGGER.trace("No element found for the text. Be sure you opened a chat window and you are authenticated.");
        }
    }

    public synchronized void setText(String text) {
        try {
            WebElement textField = driver.findElement(By.xpath("//div[@role='textbox'][@data-testid='conversation-compose-box-input']"));
            textField.sendKeys(Keys.CONTROL + "a");
            textField.sendKeys(Keys.DELETE);
            textField.sendKeys(text);
        } catch (NoSuchElementException ex) {
            LOGGER.trace("No element found for the text. Be sure you opened a chat window and you are authenticated.");
        }
    }

    public synchronized boolean search(String text) {
        return search(text, Duration.ZERO);
    }

    public synchronized boolean search(String text, Duration timeOut) {
        try {
            WebElement textField = new WebDriverWait(driver, timeOut)
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='textbox'][@data-testid='chat-list-search']")));
            textField.sendKeys(Keys.CONTROL + "a");
            textField.sendKeys(Keys.DELETE);
            textField.sendKeys(text);
            return true;
        } catch (Exception ex) {
            LOGGER.trace("No element found for the text. Be sure you opened a chat window and you are authenticated.");
        }
        return false;
    }

    public synchronized void clearTextBox() {
        try {
            WebElement textField = driver.findElement(By.xpath("//div[@role='textbox'][@data-testid='conversation-compose-box-input']"));
            textField.sendKeys(Keys.CONTROL + "a");
            textField.sendKeys(Keys.DELETE);
        } catch (NoSuchElementException ex) {
            LOGGER.trace("No element found for the text. Be sure you opened a chat window and you are authenticated.");
        }
    }

    public synchronized void printTextContent() {
        LOGGER.info(getTextContent());
    }

    public synchronized String getTextContent() {
        try {
            WebElement text = driver.findElement(By.xpath("//div[@role='textbox'][@data-testid='conversation-compose-box-input']/p/span"));
            return text.getText();
        } catch (NoSuchElementException ex) {
            LOGGER.trace("No element found for the text");
        }
        return null;
    }

    public synchronized String getConversationInfoHeader() {
        try {
            WebElement text = driver.findElement(By.xpath("//span[@data-testid='conversation-info-header-chat-title']"));
            return text.getText();
        } catch (NoSuchElementException ex) {
            LOGGER.trace("No element found for the text");
        }
        return "${missing element}";
    }

    public synchronized boolean send(int timeOutInSeconds) {
        try {
            WebElement sendButton = new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-testid='compose-btn-send']")));
            LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, sendButton));
            sendButton.click();
            return true;
        } catch (Exception ex) {
            LOGGER.trace("Couldn't send message", ex);
        }
        return false;
    }

    public synchronized boolean clickElement(By by) {
        try {
            WebElement element = driver.findElement(by);
            element.click();
            return true;
        } catch (NoSuchElementException ex) {
            LOGGER.trace("Couldn't find element...", ex);
        }
        return false;
    }

    public synchronized boolean isQRCodeVisible(int timeOutInSeconds) {
        try {
            WebElement qrCode = new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//canvas[@role='img']")));
            LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, qrCode));
            return true;
        } catch (Exception ex) {
            LOGGER.trace("Couldn't find qrCode", ex);
        }
        return false;
    }

    public synchronized boolean openChatWithUnreadNotification() {
        try {
            WebElement span = driver.findElement(By.xpath("//span[@data-testid='icon-unread-count']"));
            WebElement listItem = span.findElement(By.xpath("./ancestor::div[contains(@data-testid,'list-item-')]"));
            LOGGER.trace(WhatsAppHelper.getAttributesOfElement(driver, listItem));
            listItem.click();
            return true;
        } catch (Exception ex) {
            LOGGER.trace("Couldn't find a list element with icon-unread-count.", ex);
        }
        return false;
    }

    public synchronized List<WebElement> getSpansWithUnreadNotification() {
        try {
            return driver.findElements(By.xpath("//span[@data-testid='icon-unread-count']"));
        } catch (Exception ex) {
            LOGGER.trace("Couldn't find a list element with icon-unread-count.", ex);
        }
        return new ArrayList<>();
    }

    public synchronized Optional<WebElement> getChatList() {
        try {
            WebElement chatlist = driver.findElement(By.xpath("//div[@data-testid='chat-list']"));
            return Optional.of(chatlist);
        } catch (Exception ex) {
            LOGGER.trace("Couldn't find chatlist.", ex);
        }
        return Optional.empty();
    }

    public synchronized boolean checkAlertUpdate() {
        try {
            getAlertUpdate();
            return true;
        } catch (NoSuchElementException ex) {
            LOGGER.trace("Alert update not found", ex);
        }
        return false;
    }

    public synchronized boolean clickAlertUpdate() {
        try {
            WebElement alertUpdate = getAlertUpdate();
            alertUpdate.click();
            return true;
        } catch (NoSuchElementException ex) {
            LOGGER.trace("Alert update not found", ex);
        }
        return false;

    }

    public synchronized WebElement getAlertUpdate() {
        return driver.findElement(By.xpath("//span[@data-testid='alert-update']"));
    }

    public synchronized WebElement getStartUpProgressBar(long timeOutInMillis) throws NoSuchElementException, TimeoutException {
        WebElement progress = new WebDriverWait(driver, Duration.ofMillis(timeOutInMillis)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//progress[not(@dir='ltr')]")));
        return progress;
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
            LOGGER.trace("Expected Timeout", ex);
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofNanos(0));
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            foundAlert = true;
        } catch (TimeoutException eTO) {
            foundAlert = false;
        }
        return foundAlert;
    }

    public synchronized WebElement isPopUpPresent(Duration duration) {
        WebDriverWait wait = new WebDriverWait(driver, duration);
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@data-testid='confirm-popup']")));
        } catch (TimeoutException eTO) {
            LOGGER.trace("No popup found...", eTO);
        }
        return null;
    }

    public synchronized void acceptAlert() {
        if (isAlertPresent()) {
            driver.switchTo().alert().accept();
        }
    }
}
