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
package de.kswmd.whatsapptool.contacts;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Kai Denzel
 */
public class ChatListBean implements Comparable<ChatListBean> {

    public enum Type {
        CONTACT,
        MESSAGE,
        HEADER
    }

    private int sort = Integer.MAX_VALUE;
    private int unreadMessages;
    private String title;
    private String time;
    private String lastMessage;
    private String listItemTestId;
    //Is it of Type Message
    private Type type;

    public ChatListBean(Type type) {
        this.type = type;
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

    public String getListItemTestId() {
        return listItemTestId;
    }

    public void setListItemTestId(String listItemTestId) {
        this.listItemTestId = listItemTestId;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
        sb.append(StringUtils.trimToEmpty(time));
        sb.append("\n");
        sb.append(StringUtils.trimToEmpty(lastMessage));
        sb.append("\n");
        sb.append("translateY=");
        sb.append(sort);
        sb.append("\n");
        sb.append("Type=");
        sb.append(type.toString().toLowerCase());
        sb.append("\n############################################################");
        return sb.toString();
    }

    @Override
    public int compareTo(ChatListBean o) {
        return Integer.compare(sort, o.sort);
    }

}
