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

import de.kswmd.whatsapptool.WhatsAppClient;
import de.kswmd.whatsapptool.WhatsAppHelper;
import de.kswmd.whatsapptool.contacts.ChatListBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Kai Denzel
 */
public class CommandPrintChatList extends Command {

    private static final Logger LOGGER = LogManager.getLogger();

    private final WhatsAppClient client;

    public CommandPrintChatList(WhatsAppClient client) {
        super(COMMAND_PRINT_CHATLIST, "Prints the current chatlist to the console.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        Optional<WebElement> optionalChatList = client.getChatList();
        client.waitForTimeOut(Duration.ofMillis(500));
        if (optionalChatList.isPresent()) {
            WebElement chatList = optionalChatList.get();
            List<ChatListBean> list = WhatsAppHelper.generateFromWebElement(chatList);
            StringBuilder sb = new StringBuilder();
            list.forEach(c -> {
                sb.append(c);
                sb.append("\n");
            });
            Console.writeLine(sb.toString().trim());
            return Optional.of(list);
        }
        return Optional.empty();
    }
}
