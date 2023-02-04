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
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.StringUtils;
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
 * Wrapper class for the system.out to format the message before sent
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

    private volatile long curserPosition = 1;

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

    public synchronized static Console getInstance() {
        return instance;
    }

    public long getCurserPosition() {
        return curserPosition;
    }

    private synchronized void w(String specialEscapeString, Object o) {
        String s = String.valueOf(o);
        String output = s;
        if (showLines) {
            long curserPositionTmp = getInstance().getCurserPosition();
            int index = output.indexOf(LINE_BREAK);
            int beginIndex = 0;
            int cursorIndex = 0;
            StringBuilder sb = new StringBuilder();
            while (index != -1) {
                String sub = output.substring(beginIndex, index);
                sb
                        .append(CARRIAGE_RETURN)
                        .append(String.format(LINE_NUMBER_FORMAT, curserPositionTmp + cursorIndex))
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
                        .append(String.format(LINE_NUMBER_FORMAT, curserPositionTmp + cursorIndex))
                        .append(LINE_PREFIX)
                        .append(sub.replaceAll(String.valueOf(CARRIAGE_RETURN), "").replaceAll(String.valueOf(LINE_BREAK), ""));
                output = sb.toString();
            } else {
                if (output.contains(String.valueOf(CARRIAGE_RETURN))) {
                    output = output.replaceAll(String.valueOf(CARRIAGE_RETURN), CARRIAGE_RETURN + String.format(LINE_NUMBER_FORMAT, curserPositionTmp + cursorIndex) + LINE_PREFIX);
                } else {
                    output = CARRIAGE_RETURN + String.format(LINE_NUMBER_FORMAT, curserPositionTmp + cursorIndex) + LINE_PREFIX + output;
                }
            }
        }
        int count = StringUtils.countMatches(s, LINE_BREAK);
        instance.addToCurserPositionNumber(count);
        instance.addToLineNumber(count);
        writeRaw(specialEscapeString + output);
    }

    private synchronized void writeRaw(Object o) {
        System.out.print(o);
    }

    public static synchronized void write(String special, Object o) {
        getInstance().w(special, o);
    }

    public static synchronized void write(Object o) {
        getInstance().w("", o);
    }

    public static synchronized void writeLine(Object o) {
        getInstance().setCursorDownAndWriteLine(String.valueOf(o));
    }

    public static synchronized void writeLine() {
        getInstance().setCursorDownAndWriteLine("");
    }

    public static synchronized void writeAndMoveCursor(Object o, int cursorOffset, CursorMovement movement) {
        switch (movement) {
            case UP:
                instance.setCursorPosition((instance.curserPosition - cursorOffset));
                write(instance.getMoveCursorUpString(cursorOffset), o);
                break;
            case DOWN:
                instance.setCursorPosition((instance.curserPosition + cursorOffset));
                write(instance.getMoveCursorDownString(cursorOffset), o);
                break;
            default:
                write("", o);
                break;
        }
    }

    public static String readLine() throws UserInterruptException {
        long curserPos = getInstance().setCursorDown();
        String lineNumber = getInstance().showLines ? CARRIAGE_RETURN + String.format(LINE_NUMBER_FORMAT, getInstance().curserPosition) + LINE_PREFIX : "";
        String line = getInstance().lineReader.readLine(lineNumber + PROMPT);
        getInstance().addToCurserPositionNumber(1);
        getInstance().addToLineNumber(1);
        return line;
    }

    public synchronized void initLineReader(String[] commands) {
        StringsCompleter completer = new StringsCompleter(commands);
        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();
        // Create autopair widgets
        AutopairWidgets autopairWidgets = new AutopairWidgets(lineReader);
        // Enable autopair 
        autopairWidgets.enable();
    }

    public synchronized void clear() {
        terminal.puts(Capability.clear_screen);
        //terminal.flush();
    }

    public synchronized void moveCursorUp(int count, Object append) {
        setCursorPosition((curserPosition - count));
        String moveUpString = count > 0 ? getMoveCursorUpString(count) : "";
        write(moveUpString, append);
    }

    public synchronized void moveCursorUp(int count) {
        moveCursorUp(count, "");
    }

    public synchronized void moveCursorDown(int count, Object append) {
        setCursorPosition((curserPosition + count));
        String moveDown = count > 0 ? getMoveCursorDownString(count) : "";
        write(moveDown, append);
    }

    public synchronized void moveCursorDown(int count) {
        moveCursorDown(count, "");
    }

    public synchronized void clearCurrentLine() {
        write(getEraseLineString(), "");
    }

    /**
     *
     * @return the new curserPosition if it is not down
     */
    public synchronized long setCursorDown() {
        return setCursorDownAndWrite("");
    }

    public synchronized long setCursorDownAndWriteLine(Object append) {
        return setCursorDownAndWrite(String.valueOf(append) + String.valueOf(LINE_BREAK));
    }

    /**
     * should be used if some thread is writing for example a Progressbar at
     * position x and you want to write at the end
     *
     * @param append
     * @return the new curserPosition if it is not down
     */
    public synchronized long setCursorDownAndWrite(Object append) {
        int offset = (int) (lineNumber - curserPosition);
        moveCursorDown(offset, append);
        return curserPosition;
    }

    private synchronized void setCursorPosition(long position) {
        this.curserPosition = position;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public String getMoveCursorUpString(int count) {
        return ESCAPE_CHAR + "[" + count + "A";
    }

    public String getMoveCursorDownString(int count) {
        return ESCAPE_CHAR + "[" + count + "B";
    }

    public String getEraseLineString() {
        return "\33[2K";
    }

    private synchronized void addToLineNumber(int lines) {
        lineNumber = lineNumber + lines;
    }

    private synchronized void addToCurserPositionNumber(int lines) {
        curserPosition = curserPosition + lines;
    }

    @Override
    public void append(LogEvent event) {
        readLock.lock();
        try {
            final byte[] bytes = getLayout().toByteArray(event);
            getInstance().setCursorDownAndWrite(new String(bytes));
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
