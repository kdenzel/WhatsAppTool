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
package de.kswmd.whatsapptool.text;

import de.kswmd.whatsapptool.cli.Console;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;

/**
 * Class that interprets text and replaces variables like [date] for the current
 * date
 *
 * @author Kai Denzel
 */
public class MessageParser {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    public enum KeyWord {
        DATE("date"),
        TIME("time");
        
        private final String word;
        
        KeyWord(String word) {
            this.word = word;
        }
        
        public String getWord() {
            return word;
        }
        
        @Override
        public String toString() {
            return word;
        }
    }
    
    private final String regex;
    private final Pattern pattern;
    private final Map<String, Object> replacements = new HashMap<>();
    
    public static final MessageParser DEFAULT_PARSER = new MessageParser("[^\\\"'](\\[((\\w+):(.+?)|(\\w+))\\])[^\\\"']");
    
    private MessageParser(final String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex, Pattern.MULTILINE);
    }

    /**
     * Searches the text for replacement variables
     *
     * @param text
     * @return
     */
    public synchronized String format(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        Matcher matcher = pattern.matcher(text);
        LocalDateTime now = LocalDateTime.now();
        replacements.put(KeyWord.DATE.getWord(), now);
        replacements.put(KeyWord.TIME.getWord(), now);
        int i = 0;
        while (matcher.find()) {
            String itemId = matcher.group(3);
            if (itemId == null) {
                itemId = matcher.group(2);
            }
            Object replacement = null;
            try {
                KeyWord kw = KeyWord.valueOf(itemId.toUpperCase());
                String itemAttach = matcher.group(4);
                switch (kw) {
                    case TIME:
                    case DATE:
                        LocalDateTime date = (LocalDateTime) replacements.get(itemId);
                        if (itemAttach != null) {
                            replacement = date.format(DateTimeFormatter.ofPattern(itemAttach));
                        } else {
                            replacement = date;
                        }
                        break;
                    
                }
            } catch (Exception ex) {
                LOGGER.trace("Error in parsing " + matcher.group(1), ex);
            }
            builder.append(text.substring(i, matcher.start(1)));
            if (replacement == null) {
                builder.append(matcher.group(1));
            } else {
                builder.append(replacement);
            }
            i = matcher.end(1);
        }
        builder.append(text.substring(i, text.length()));
        return builder.toString().replaceAll(String.valueOf(Console.LINE_BREAK), Keys.chord(Keys.SHIFT,Keys.ENTER));
    }
    
}
