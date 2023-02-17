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

import de.kswmd.whatsapptool.WhatsAppHelper.Emoji;
import de.kswmd.whatsapptool.contacts.Message;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that interprets text and replaces variables like [date] for the current
 * date
 *
 * @author Kai Denzel
 */
public class MessageParser {

    private static final Logger LOGGER = LogManager.getLogger();

    public enum KeyWord {
        DATE,
        TIME,
        EMOJI,
        IDENTIFIER,
        CRONEXPRESSION;
    }

    private final Pattern parsingPattern;
    private final Pattern matchBlocksInsideQuotesPattern;
    private final Pattern matchEscapedQuotesPattern;
    private final Map<KeyWord, Object> keywords = new HashMap<>();
    private final Map<String, String> placeholders = new LinkedHashMap<>();

    public static final MessageParser DEFAULT_PARSER = new MessageParser("\\[((\\w+):(.+?)|(\\w+))\\]");

    private MessageParser(final String regex) {
        this.parsingPattern = Pattern.compile(regex, Pattern.MULTILINE);
        this.matchBlocksInsideQuotesPattern = Pattern.compile("\\\"(.*?)\\\"", Pattern.MULTILINE | Pattern.DOTALL);
        this.matchEscapedQuotesPattern = Pattern.compile("\\\\(\")", Pattern.MULTILINE);

    }

    private String replaceSpecialCharactersWithPlaceHolders(String origText, Pattern pattern) {
        Matcher matcher = pattern.matcher(origText);
        StringBuilder newText = new StringBuilder(origText.length());
        int i = 0;
        while (matcher.find()) {
            UUID uuid = UUID.randomUUID();
            String before = origText.substring(i, matcher.start());
            String key = uuid.toString() + placeholders.size();
            placeholders.put(key, matcher.group(1));
            newText.append(before).append(key);
            i = matcher.end();
        }
        newText.append(origText.substring(i, origText.length()));
        return newText.toString();
    }
    
    public String format(Message m){
        keywords.put(KeyWord.IDENTIFIER, m.getEntity().getIdentifier());
        keywords.put(KeyWord.CRONEXPRESSION, m.getCronExpressionString());
        return format(m.getContent());
    }

    /**
     * Searches the text for replacement variables
     *
     * @param origText
     * @return
     */
    public String format(String origText) {
        String newText = replaceSpecialCharactersWithPlaceHolders(origText, matchEscapedQuotesPattern);
        newText = replaceSpecialCharactersWithPlaceHolders(newText, matchBlocksInsideQuotesPattern);
        StringBuilder builder = new StringBuilder(newText.length());
        LocalDateTime now = LocalDateTime.now();
        keywords.put(KeyWord.DATE, now);
        keywords.put(KeyWord.TIME, now);
        Matcher matcher = parsingPattern.matcher(newText);
        int i = 0;
        while (matcher.find()) {
            String itemId = matcher.group(4);
            if (itemId == null) {
                itemId = matcher.group(2);
            }
            Object replacement = null;
            try {
                KeyWord kw = KeyWord.valueOf(itemId.toUpperCase());
                String itemAttach = matcher.group(3);
                switch (kw) {
                    case TIME:
                        DateTimeFormatter dtf;
                        LocalDateTime date = (LocalDateTime) keywords.get(kw);
                        if (itemAttach != null) {
                            dtf = DateTimeFormatter.ofPattern(itemAttach);
                        } else {
                            dtf = DateTimeFormatter.ISO_TIME;
                        }
                        replacement = date.format(dtf);
                        break;
                    case DATE:
                        date = (LocalDateTime) keywords.get(kw);
                        if (itemAttach != null) {
                            dtf = DateTimeFormatter.ofPattern(itemAttach);
                        } else {
                            dtf = DateTimeFormatter.ISO_DATE;
                        }
                        replacement = date.format(dtf);
                        break;
                    case EMOJI:
                        Emoji emoji = Emoji.valueOf(itemAttach.toUpperCase());
                        replacement = emoji.getSequence();
                        break;
                    case IDENTIFIER:
                        replacement = keywords.get(kw);
                        break;
                    case CRONEXPRESSION:
                        replacement = keywords.get(kw);
                        break;
                }
            } catch (Exception ex) {
                LOGGER.trace("Error in parsing " + matcher.group(), ex);
            }
            builder.append(newText.substring(i, matcher.start(0)));
            if (replacement == null) {
                builder.append(matcher.group());
            } else {
                builder.append(replacement);
            }
            i = matcher.end();
        }

        builder.append(newText.substring(i, newText.length()));
        Entry[] entries = this.placeholders.entrySet().toArray(Entry[]::new);
        for (i = entries.length - 1; i >= 0; i--) {
            Entry e = entries[i];
            String key = String.valueOf(e.getKey());
            String value = String.valueOf(e.getValue());
            int startIndex = builder.indexOf(key);
            builder.replace(startIndex, startIndex + key.length(), value);
        }
        keywords.clear();
        return builder.toString();
    }

}
