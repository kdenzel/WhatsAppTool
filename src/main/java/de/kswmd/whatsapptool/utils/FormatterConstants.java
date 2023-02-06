/*
 * The MIT License
 *
 * Copyright 2023 kdenzel.
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

import java.time.format.DateTimeFormatter;

/**
 * Class that stores date formatter used a lot.
 *
 * @author kdenzel
 */
public final class FormatterConstants {

    public static final DateTimeFormatter DATE_TIME_FORMAT_LOCALE_DE = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMAT_YYYY_MM = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter DATE_FORMAT_MM_dd_YYYY = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    public static final DateTimeFormatter TIME_FORMAT_HH_mm_ss_SSS = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

    private FormatterConstants() {
    }
}
