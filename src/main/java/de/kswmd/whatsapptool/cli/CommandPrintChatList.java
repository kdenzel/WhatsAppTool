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
    private final List<ChatListBean> list = new ArrayList<>();

    public CommandPrintChatList(WhatsAppClient client) {
        super(COMMAND_PRINT_CHATLIST, "Prints the current chatlist to the console.");
        this.client = client;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        Optional<WebElement> optionalChatList = client.getChatList();
        if (optionalChatList.isPresent()) {
            WebElement chatList = optionalChatList.get();
            List<WebElement> listItems = chatList.findElements(By.xpath(".//div[contains(@data-testid,'list-item-')]"));
            for (WebElement listItem : listItems) {
                String sort = listItem.getAttribute("style");
                sort = sort.replaceAll("^.*translateY[(](.*)px[)].*$", "$1");
                WebElement timeDiv = listItem.findElement(By.cssSelector("div.Dvjym"));
                WebElement title = listItem.findElement(By.xpath(".//span[@dir='auto']"));
                WebElement lastMessageStatus = listItem.findElement(By.xpath(".//span[@data-testid='last-msg-status']"));
                int unreadMessages = 0;
                try {
                    WebElement unreadCountSpan = listItem.findElement(By.xpath(".//span[@data-testid='icon-unread-count']"));
                    unreadMessages = Integer.parseInt(unreadCountSpan.getText());
                } catch (NoSuchElementException ex) {
                    LOGGER.trace("Element span unread count not found...", ex);
                } catch (NumberFormatException ex) {
                    LOGGER.trace("The text value of span element was not an integer...", ex);
                }

                int index = listItems.indexOf(listItem);
                ChatListBean bean;
                if (index < list.size()) {
                    bean = list.get(index);
                } else {
                    bean = new ChatListBean();
                    list.add(bean);
                }
                bean.setTitle(title.getText());
                bean.setTime(timeDiv.getText());
                bean.setLastMessage(lastMessageStatus.getText().replaceAll("\n", ""));
                bean.setUnreadMessages(unreadMessages);
                try {
                    bean.setSort(Integer.parseInt(sort));
                } catch (NumberFormatException ex) {
                    LOGGER.trace("Sort value invalid...", ex);
                }
            }
            if (listItems.size() < list.size()) {
                list.subList(listItems.size() - 1, list.size() - 1).clear();
            }
            Collections.sort(list);
            StringBuilder sb = new StringBuilder();
            list.forEach(c -> {
                sb.append(c);
                sb.append("\n");
            });
            Console.writeLine(sb.toString().trim());
        }
        return Optional.empty();
    }

    private class ChatListBean implements Comparable<ChatListBean> {

        private int sort = Integer.MAX_VALUE;
        private int unreadMessages;
        private String title;
        private String time;
        private String lastMessage;

        public ChatListBean() {
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public int getUnreadMessages() {
            return unreadMessages;
        }

        public void setUnreadMessages(int unreadMessages) {
            this.unreadMessages = unreadMessages;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-50s", title));
            if (unreadMessages > 0) {
                sb.append("(");
                sb.append(unreadMessages);
                sb.append(") ");
            }
            sb.append(time);
            sb.append("\n");
            sb.append(lastMessage);
            sb.append("\n");
            sb.append("translateY=");
            sb.append(sort);
            sb.append("\n############################################################");
            return sb.toString();
        }

        @Override
        public int compareTo(ChatListBean o) {
            return Integer.compare(sort, o.sort);
        }

    }

}
