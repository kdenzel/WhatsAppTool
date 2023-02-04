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
package de.kswmd.whatsapptool.utils;

import de.kswmd.whatsapptool.cli.Console;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Do not use Log4j in this class, because it is used to set the Log4j log file
 * path before any logging happens
 *
 * @author Kai Denzel
 */
public class PathResolver {

    private PathResolver() {
    }

    public static Path getJarFilePathOrWorkingDirectory() {
        URL classResource = PathResolver.class.getResource(PathResolver.class.getSimpleName() + ".class");
        if (classResource == null) {
            throw new RuntimeException("class resource is null");
        }
        String url = classResource.toString();
        if (url.startsWith("jar:file:")) {
            // extract 'file:......jarName.jar' part from the url string
            String path = url.replaceAll("^jar:(file:.*)!/.*", "$1");
            try {
                return Paths.get(new URI(path)).getParent();
            } catch (URISyntaxException ex) {
                Console.writeLine("invalid path=" + path);
            }
        }
        return Paths.get(System.getProperty("user.dir"));
    }

    public static Path getConfigDir() {
        return Paths.get(getJarFilePathOrWorkingDirectory().toString(), "config");
    }

    public static Path getDefaultChromeBrowserProfileDir() {
        return Paths.get(getJarFilePathOrWorkingDirectory().toString(), "tmp", "profileChrome");
    }

    public static Path getDefaultFirefoxBrowserProfileDir() {
        return Paths.get(getJarFilePathOrWorkingDirectory().toString(), "tmp", "profileFirefox");
    }
}
