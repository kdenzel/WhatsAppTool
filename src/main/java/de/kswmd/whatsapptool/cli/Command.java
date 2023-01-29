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

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Kai Denzel
 */
public abstract class Command implements Comparable<Command> {

    public static final String COMMAND_EXIT = "exit";
    public static final String COMMAND_HELP = "help";
    public static final String COMMAND_SEND = "send";
    public static final String COMMAND_OPEN = "open";
    public static final String COMMAND_CHECK_LOGGEDIN = "check_login";
    public static final String COMMAND_SHOW_TEXT = "show_text";
    public static final String COMMAND_SET_TEXT = "set_text";
    public static final String COMMAND_PAUSE_JOB = "pause_job";
    public static final String COMMAND_RESUME_JOB = "resume_job";
    public static final String COMMAND_SHOW_INFO_HEADER = "show_info_header";
    public static final String COMMAND_CLICK_NOTIFICATION = "click_notification";
    public static final String COMMAND_SHOW_NOTIFICATIONS = "show_notifications";
    public static final String COMMAND_PRINT_CHATLIST = "print_chatlist";
    public static final String COMMAND_CHECK_UPDATE = "check_update";
    public static final String COMMAND_UPDATE = "update";
    public static final String COMMAND_CLEAR = "clear";
    public static final String COMMAND_REFRESH = "refresh";
    public static final String COMMAND_RELOAD_NOTIFICATIONS_JOB = "reload_notification_jobs";

    private final String command;
    private final String description;

    public Command(String c, String d) {
        this.command = c;
        this.description = d;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public abstract Optional<Object> execute(Object parameters);

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.command);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Command other = (Command) obj;
        return Objects.equals(this.command, other.command);
    }

    @Override
    public int compareTo(Command o) {
        return command.compareTo(o.command);
    }

}
