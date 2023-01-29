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

import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Kai Denzel
 */
public class CommandHelp extends Command {

    private final Set<Command> commands;

    public CommandHelp(Set<Command> commands) {
        super(COMMAND_HELP, "Prints this dialog.");
        this.commands = commands;
    }

    @Override
    public Optional<Object> execute(Object parameters) {
        StringBuilder sb = new StringBuilder();
        for (Command c : commands) {
            sb.append(String.format("%-30s", c.getCommand()));
            sb.append(String.format("%s", c.getDescription()));
            sb.append("\n");
            
        }
        Console.writeLine(sb.toString().trim());
        return Optional.empty();
    }

}
