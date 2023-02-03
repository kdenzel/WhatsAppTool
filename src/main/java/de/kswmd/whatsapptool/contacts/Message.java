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

import java.text.ParseException;
import org.quartz.CronExpression;

/**
 *
 * @author Kai Denzel
 */
public final class Message {

    /**
     * Can be a Name (For example a group name) or Phonenumber.
     */
    private CronExpression cronExpression;
    private String cronExpressionString;
    private String content;
    private Entity entity;

    public Message() {
    }

    public Message(String cronExpression, String message) throws ParseException {
        setCronExpression(cronExpression);
        this.content = message;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public String getCronExpressionString() {
        return cronExpressionString;
    }

    public void setCronExpression(String cronExpression) throws ParseException {
        this.cronExpression = new CronExpression(cronExpression);
        this.cronExpressionString = cronExpression;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(entity)
                .append(":\n")
                .append(cronExpression)
                .append("\n\"")
                .append(content)
                .append("\"");
        return sb.toString();
    }
}
