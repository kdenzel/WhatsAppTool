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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

/**
 *
 * @author Kai Denzel
 */
public class CommandSearchContacts extends Command {

    private static final Logger LOGGER = LogManager.getLogger();
    private final WhatsAppClient client;
    private final List<ChatListBean.Type> filters;

    public CommandSearchContacts(final WhatsAppClient client) {
        super(COMMAND_SEARCH_CONTACTS, "Searches all contacts matching with parameter. You can also filter with -f or --filter for " + Arrays.toString(ChatListBean.Type.values()));
        this.client = client;
        this.filters = new ArrayList<>(ChatListBean.Type.values().length);
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        String params = String.valueOf(parameters);
        String search = params;
        filters.clear();
        if (params.startsWith("-f") || params.startsWith("--filter")) {

            int indexOfFirstSpace = params.indexOf(" ");
            if (indexOfFirstSpace > 0) {
                int idnexOfSecondSpace = params.indexOf(" ", indexOfFirstSpace + 1);
                if (idnexOfSecondSpace < 0) {
                    idnexOfSecondSpace = params.length();
                }
                String filterString = params.substring(indexOfFirstSpace, idnexOfSecondSpace).trim();
                try {
                    String[] filterArray = filterString.split(",");
                    for (String filter : filterArray) {
                        ChatListBean.Type type = ChatListBean.Type.valueOf(filter.toUpperCase());
                        filters.add(type);
                    }
                    search = params.substring(idnexOfSecondSpace);
                } catch (IllegalArgumentException ex) {
                    LOGGER.debug("Wrong value for filter", ex);
                }

            }
            if (filters.isEmpty()) {
                search = null;
                Console.writeLine("No results found: Filter values are " + Arrays.toString(ChatListBean.Type.values()));
            }
        }
        if (!StringUtils.trimToEmpty(search).isEmpty()) {
            client.search(search);
            client.waitForTimeOut(Duration.ofMillis(500));
            Optional<WebElement> optonalChatList = client.getChatList();
            if (optonalChatList.isPresent()) {
                WebElement chatList = optonalChatList.get();
                List<ChatListBean> list = WhatsAppHelper.generateFromWebElement(chatList);
                if (!filters.isEmpty()) {
                    list = list.stream().filter(cb -> filters.contains(cb.getType())).collect(Collectors.toList());
                }
                StringBuilder sb = new StringBuilder();
                list.forEach(c -> {
                    sb.append(c);
                    sb.append("\n");
                });
                Console.writeLine(sb.toString().trim());
                return Optional.of(list);
            } else {
                Console.writeLine("No results found.");
            }
        } else {
            Console.writeLine("No search term found.");
        }
        return Optional.empty();
    }

}
