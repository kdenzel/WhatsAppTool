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

import java.time.Duration;

/**
 * Class for storing Durations that are used a lot.
 *
 * @author Kai Denzel
 */
public final class ChronoConstants {

    /**
     * Millis
     */
    public static final Duration DURATION_OF_50_MILLIS = Duration.ofMillis(50);
    public static final Duration DURATION_OF_500_MILLIS = Duration.ofMillis(500);
    /**
     * Seconds
     */
    public static final Duration DURATION_OF_1_SECOND = Duration.ofSeconds(1);
    public static final Duration DURATION_OF_2_SECONDS = Duration.ofSeconds(2);
    public static final Duration DURATION_OF_3_SECONDS = Duration.ofSeconds(3);
    public static final Duration DURATION_OF_5_SECONDS = Duration.ofSeconds(5);
    public static final Duration DURATION_OF_10_SECONDS = Duration.ofSeconds(10);
    public static final Duration DURATION_OF_15_SECONDS = Duration.ofSeconds(15);
    public static final Duration DURATION_OF_30_SECONDS = Duration.ofSeconds(30);
    

    private ChronoConstants() {
    }

}
