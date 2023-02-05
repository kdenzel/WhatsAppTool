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
package de.kswmd.whatsapptool.cli;

import de.kswmd.whatsapptool.WhatsAppWebClient;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Kai Denzel
 */
public class CommandShowNotifications extends Command {

    private WhatsAppWebClient client;

    public CommandShowNotifications(WhatsAppWebClient client) {
        super(COMMAND_SHOW_NOTIFICATIONS, "Shows all contacts with icon unread.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        List<WebElement> spans = client.getSpansWithUnreadNotification();
        for (WebElement span : spans) {
            WebElement listItem = span.findElement(By.xpath("./ancestor::div[contains(@data-testid,'list-item-')]"));
            WebElement spanWithTitle = listItem.findElement(By.xpath(".//span[@dir='auto']"));
            Console.writeLine(spanWithTitle.getText());
        }
        return Optional.empty();
    }

}
