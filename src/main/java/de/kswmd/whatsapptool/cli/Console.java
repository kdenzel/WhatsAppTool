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

import de.kswmd.whatsapptool.utils.Environment;
import de.kswmd.whatsapptool.utils.FormatterConstants;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.jline.widget.AutopairWidgets;

/**
 * Wrapper class for the system.out to format the message before sent It can
 * also be used as a log4j Appender.
 *
 * @author Kai Denzel
 */
@Plugin(name = "AdvancedConsole", category = "Core", elementType = "appender", printObject = true)
public final class Console extends AbstractAppender {

    public enum CursorMovement {
        NONE,
        DOWN,
        UP
    }

    public static final String PROMPT;

    public static final char CARRIAGE_RETURN = '\r';
    public static final char ESCAPE_CHAR = '\u001b';
    public static final char LINE_BREAK = '\n';
    public static final String LINE_PREFIX = "| ";
    public static final String LINE_NUMBER_FORMAT = "%02d";

    static {
        PROMPT = Environment.getInstance().getProperty("artifactId") + "-v." + Environment.getInstance().getProperty("version") + "-" + System.getProperty("user.name") + "> ";
        LogManager.getLogger();
    }

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    private Terminal terminal;
    private LineReader lineReader;

    private static Console instance;

    private volatile long lineNumber = 1;

    private volatile long cursorPosition = 1;

    private final boolean showLines;

    protected Console(String name, Filter filter,
            Layout<? extends Serializable> layout, boolean ignoreExceptions, boolean showLineNumbers) {
        super(name, filter, layout, ignoreExceptions);
        this.showLines = showLineNumbers;
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
        } catch (IOException ex) {
            LOGGER.error("Couldn't create terminal or line reader...", ex);
        }
    }

    private synchronized void w(Object o) {
        w("", "", o, "");
    }

    private synchronized void w(String specialCodeBefore, Object o) {
        w("", specialCodeBefore, o, "");
    }

    private synchronized void w(String specialCodeBefore, Object o, String specialCodeAfter) {
        w("", specialCodeBefore, o, specialCodeAfter);
    }

    private synchronized void w(String cursorMovement, String specialCodeBefore, Object o, String specialCodeAfter) {
        String s = String.valueOf(o);
        String output = s;
        if (showLines) {
            long cursorPositionTmp = instance.cursorPosition;
            int index = output.indexOf(LINE_BREAK);
            int beginIndex = 0;
            int cursorIndex = 0;
            StringBuilder sb = new StringBuilder();
            while (index != -1) {
                String sub = output.substring(beginIndex, index);
                sb
                        .append(CARRIAGE_RETURN)
                        .append(String.format(LINE_NUMBER_FORMAT, cursorPositionTmp + cursorIndex))
                        .append(LINE_PREFIX)
                        .append(sub.replaceAll(String.valueOf(CARRIAGE_RETURN), "").replaceAll(String.valueOf(LINE_BREAK), ""))
                        .append(LINE_BREAK);
                beginIndex = index;
                index = output.indexOf(LINE_BREAK, index + 1);
                cursorIndex++;
            }
            if (sb.length() > 0) {
                String sub = output.substring(output.lastIndexOf(LINE_BREAK));
                sb
                        .append(CARRIAGE_RETURN)
                        .append(String.format(LINE_NUMBER_FORMAT, cursorPositionTmp + cursorIndex))
                        .append(LINE_PREFIX)
                        .append(sub.replaceAll(String.valueOf(CARRIAGE_RETURN), "").replaceAll(String.valueOf(LINE_BREAK), ""));
                output = sb.toString();
            } else {
                if (output.contains(String.valueOf(CARRIAGE_RETURN))) {
                    output = output.replaceAll(String.valueOf(CARRIAGE_RETURN), CARRIAGE_RETURN + String.format(LINE_NUMBER_FORMAT, cursorPositionTmp + cursorIndex) + LINE_PREFIX);
                } else {
                    output = CARRIAGE_RETURN + String.format(LINE_NUMBER_FORMAT, cursorPositionTmp + cursorIndex) + LINE_PREFIX + output;
                }
            }
        }
        int count = StringUtils.countMatches(s, LINE_BREAK);
        instance.addToCurserPositionNumber(count);
        instance.addToLineNumber(count);
        writeRaw(cursorMovement + specialCodeBefore + output + specialCodeAfter);
    }

    private synchronized void writeRaw(Object o) {
        System.out.print(o);
    }

    /**
     * writes to the last line and returns the current cursor position
     *
     * @param o
     * @return
     */
    public static synchronized long write(Object o) {
        return write("", o, "");
    }

    /**
     * writes to the last line and returns the current cursor position
     *
     * @param specialCodeBefore
     * @param o
     * @return
     */
    public static synchronized long write(String specialCodeBefore, Object o) {
        return write(specialCodeBefore, o, "");
    }

    /**
     * writes to the last line and returns the current cursor position
     *
     * @param specialCodeBefore
     * @param o
     * @param specialCodeAfter
     * @return
     */
    public static synchronized long write(String specialCodeBefore, Object o, String specialCodeAfter) {
        return write("", specialCodeBefore, o, specialCodeAfter);
    }

    /**
     * writes to the current line and moves the cursor depending on the
     * corserMovement-String
     *
     * @param cursorMovement
     * @param specialCodeBefore
     * @param o
     * @param specialCodeAfter
     * @return
     */
    public static synchronized long write(String cursorMovement, String specialCodeBefore, Object o, String specialCodeAfter) {
        instance.w(cursorMovement, specialCodeBefore, o, specialCodeAfter);
        return instance.cursorPosition;
    }

    /**
     * Sets cursor down and writes at end.
     *
     * @param specialCodeBefore
     * @param o
     * @param specialCodeAfter
     * @return
     */
    public static synchronized long writeAtEnd(String specialCodeBefore, Object o, String specialCodeAfter) {
        return instance.setCursorDownAndWrite(specialCodeBefore, o, specialCodeAfter);
    }

    /**
     * Sets cursor down and writes at end.
     *
     * @param o
     * @return
     */
    public static synchronized long writeAtEnd(Object o) {
        return instance.setCursorDownAndWrite("", o, "");
    }

    /**
     * writes to the last line with a line break and returns the current cursor
     * position
     *
     * @return
     */
    public static synchronized long writeLine() {
        return instance.setCursorDownAndWriteLine("");
    }

    /**
     * writes to the last line with a line break and returns the current cursor
     * position
     *
     * @param o
     * @return
     */
    public static synchronized long writeLine(Object o) {
        return instance.setCursorDownAndWriteLine(String.valueOf(o));
    }

    /**
     * writes to the last line with a line break and returns the current cursor
     * position
     *
     * @param specialCodeBefore
     * @param o
     * @param specialCodeAfter
     * @return
     */
    public static synchronized long writeLine(String specialCodeBefore, Object o, String specialCodeAfter) {
        return instance.setCursorDownAndWriteLine(specialCodeBefore, String.valueOf(o), specialCodeAfter);
    }

    /**
     * Writes from the current cursor position with offset in relation to the
     * movement and adjusts the current cursor position to the new position.
     *
     * @param cursorOffset
     * @param append
     * @param movement
     */
    public static synchronized void writeAndMoveCursor(int cursorOffset, Object append, CursorMovement movement) {
        writeAndMoveCursor(cursorOffset, "", append, "", movement);
    }

    /**
     * Writes from the current cursor position with offset in relation to the
     * movement and adjusts the current cursor position to the new position.
     *
     * @param cursorOffset
     * @param specialCodeBefore
     * @param append
     * @param specialCodeAfter
     * @param movement
     */
    public static synchronized void writeAndMoveCursor(int cursorOffset, String specialCodeBefore, Object append, String specialCodeAfter, CursorMovement movement) {
        switch (movement) {
            case UP:
                instance.setCursorPosition((instance.cursorPosition - cursorOffset));
                write(Console.getMoveCursorUpString(cursorOffset), specialCodeBefore, append, specialCodeAfter);
                break;
            case DOWN:
                instance.setCursorPosition((instance.cursorPosition + cursorOffset));
                write(Console.getMoveCursorDownString(cursorOffset), specialCodeBefore, append, specialCodeAfter);
                break;
            default:
                write("", specialCodeBefore, append, specialCodeAfter);
                break;
        }
    }

    /**
     * Returns the current cursor position.
     *
     * @return
     */
    public static synchronized long getCursorPosition() {
        return instance.cursorPosition;
    }

    /**
     * Reads a new line from system.in
     *
     * @return
     * @throws UserInterruptException
     */
    public static String readLine() throws UserInterruptException {
        long cursorPos = instance.setCursorDown();
        String lineNumber = instance.showLines ? CARRIAGE_RETURN + String.format(LINE_NUMBER_FORMAT, cursorPos) + LINE_PREFIX : "";
        LocalTime lt = LocalTime.now();
        String line = instance.lineReader.readLine(lineNumber + lt.format(FormatterConstants.TIME_FORMAT_HH_mm_ss_SSS) + " " + PROMPT);
        instance.addToCurserPositionNumber(1);
        instance.addToLineNumber(1);
        return line;
    }

    /**
     * initializes the line reader with a List of commands for autocompletion
     *
     * @param commands
     */
    public static synchronized void initLineReader(String[] commands) {
        StringsCompleter completer = new StringsCompleter(commands);
        instance.lineReader = LineReaderBuilder.builder()
                .terminal(instance.terminal)
                .completer(completer)
                .build();
        // Create autopair widgets
        AutopairWidgets autopairWidgets = new AutopairWidgets(instance.lineReader);
        // Enable autopair 
        autopairWidgets.enable();
    }

    /**
     * Clears the terminal.
     */
    public static synchronized void clear() {
        instance.terminal.puts(Capability.clear_screen);
        //terminal.flush();
    }

    /**
     * Moves the cursor up and writes an empty string. Also adjusts the
     * cursorPosition.
     *
     * @param count number of lines to be moved up
     */
    private synchronized void moveCursorUpAndWrite(int count) {
        moveCursorUpAndWrite(count, "", "", "");
    }

    /**
     * Moves the cursor up and writes. Also adjusts the cursorPosition.
     *
     * @param count number of lines to be moved up
     * @param specialCodeBefore special code before
     * @param append the object to be written
     * @param specialCodeAfter special code after
     */
    private synchronized void moveCursorUpAndWrite(int count, String specialCodeBefore, Object append, String specialCodeAfter) {
        setCursorPosition((cursorPosition - count));
        String moveUpString = count > 0 ? getMoveCursorUpString(count) : "";
        write(moveUpString, specialCodeBefore, append, specialCodeAfter);
    }

    /**
     * Moves the cursor down and writes an empty string. Also adjusts the
     * cursorPosition.
     *
     * @param count number of lines to be moved down
     */
    private synchronized void moveCursorDownAndWrite(int count) {
        moveCursorDownAndWrite(count, "");
    }

    /**
     * Moves the cursor down and writes depending on append. Also adjusts the
     * cursorPosition.
     *
     * @param count number of lines to be moved down
     * @param append the object to be written
     */
    private synchronized void moveCursorDownAndWrite(int count, Object append) {
        moveCursorDownAndWrite(count, "", append, "");
    }

    /**
     * Moves the cursor down and writes depending on append. Also adjusts the
     * cursorPosition.
     *
     * @param count number of lines to be moved down
     * @param specialCodeBefore specialCode before
     * @param append the object to be written
     * @param specialCodeAfter specialCode after
     */
    private synchronized void moveCursorDownAndWrite(int count, String specialCodeBefore, Object append, String specialCodeAfter) {
        setCursorPosition((cursorPosition + count));
        String moveDown = count > 0 ? getMoveCursorDownString(count) : "";
        write(moveDown, specialCodeBefore, append, specialCodeAfter);
    }

    /**
     * Erases the current line.
     */
    private static synchronized void clearCurrentLine() {
        instance.write(getEraseLineString(), "");
    }

    /**
     * Sets the cursor down and also adjusts the cursorPosition.
     */
    private synchronized long setCursorDown() {
        return setCursorDownAndWrite("");
    }

    /**
     * Sets the cursor down with a line break, writes append and also adjusts
     * the cursorPosition.
     *
     * @param append
     * @return
     */
    private synchronized long setCursorDownAndWriteLine(Object append) {
        return setCursorDownAndWrite(String.valueOf(append) + String.valueOf(LINE_BREAK));
    }

    /**
     * Sets the cursor down with a line break, writes append and also adjusts
     * the cursorPosition.
     *
     * @param append
     * @return the new cursorPosition
     */
    private synchronized long setCursorDownAndWrite(Object append) {
        return setCursorDownAndWrite("", append, "");
    }

    /**
     * Sets the cursor down with a line break, writes append and also adjusts
     * the cursorPosition.
     *
     * @param specialCodeBefore
     * @param append
     * @param specialCodeAfter
     * @return
     */
    private synchronized long setCursorDownAndWriteLine(String specialCodeBefore, Object append, String specialCodeAfter) {
        return setCursorDownAndWrite(specialCodeBefore, String.valueOf(append) + String.valueOf(LINE_BREAK), specialCodeAfter);
    }

    /**
     * Sets the cursor down with a line break, writes append and also adjusts
     * the cursorPosition.
     *
     * @param specialCodeBefore
     * @param append
     * @param specialCodeAfter
     * @return the new cursorPosition
     */
    private synchronized long setCursorDownAndWrite(String specialCodeBefore, Object append, String specialCodeAfter) {
        int offset = (int) (lineNumber - cursorPosition);
        moveCursorDownAndWrite(offset, specialCodeBefore, append, specialCodeAfter);
        return cursorPosition;
    }

    /**
     * sets the cursor position
     *
     * @param position
     */
    private synchronized void setCursorPosition(long position) {
        this.cursorPosition = position;
    }

    /**
     * Returns the string sequence that tells the console to move the cursor up.
     *
     * @param count amount of lines to move up
     * @return
     */
    public static synchronized String getMoveCursorUpString(int count) {
        return ESCAPE_CHAR + "[" + count + "A";
    }

    /**
     * Returns the string sequence that tells the console to move the cursor
     * down.
     *
     * @param count amount of lines to move down
     * @return
     */
    public static synchronized String getMoveCursorDownString(int count) {
        return ESCAPE_CHAR + "[" + count + "B";
    }

    /**
     * Returns the string sequence that tells the console to erase the current
     * line.
     *
     * @return
     */
    public static synchronized String getEraseLineString() {
        return "\33[2K";
    }

    /**
     * Adds lines to the line number.
     *
     * @param lines
     */
    private synchronized void addToLineNumber(int lines) {
        lineNumber = lineNumber + lines;
    }

    /**
     * Adds lines to the current cursor position.
     *
     * @param lines
     */
    private synchronized void addToCurserPositionNumber(int lines) {
        cursorPosition = cursorPosition + lines;
    }

    /**
     * Called by log4j.
     *
     * @param event
     */
    @Override
    public void append(LogEvent event) {
        readLock.lock();
        try {
            final byte[] bytes = getLayout().toByteArray(event);
            String logMessage = new String(bytes);
            Level level = event.getLevel();
            if (level == Level.ERROR) {
                Console.writeAtEnd(ConsoleColors.RED, logMessage, ConsoleColors.RESET);
            } else if (level == Level.WARN) {
                Console.writeAtEnd(ConsoleColors.YELLOW, logMessage, ConsoleColors.RESET);
            } else if (level == Level.DEBUG) {
                Console.writeAtEnd(ConsoleColors.GREEN, logMessage, ConsoleColors.RESET);
            } else if (level == Level.TRACE) {
                Console.writeAtEnd(ConsoleColors.PURPLE, logMessage, ConsoleColors.RESET);
            } else {
                Console.writeAtEnd(logMessage);
            }
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        } finally {
            readLock.unlock();
        }
    }

    // Your custom appender needs to declare a factory method
    // annotated with `@PluginFactory`. Log4j will parse the configuration
    // and call this factory method to construct an appender instance with
    // the configured attributes.
    @PluginFactory
    public static Console createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("showLines") boolean showLines,
            @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for Console");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        instance = new Console(name, filter, layout, false, showLines);
        return instance;
    }
}
