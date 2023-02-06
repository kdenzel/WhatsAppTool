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

import de.kswmd.whatsapptool.MiscConstants;
import de.kswmd.whatsapptool.cli.Console;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.jline.reader.UserInterruptException;

/**
 *
 * @author Kai Denzel
 */
public class ProgressBar implements Runnable {

    private static final List<ProgressBar> pool = new ArrayList<>(10);

    private volatile Duration duration;
    private volatile ChronoUnit unit;
    private boolean finished;
    private volatile long curserPosition;
    private Thread t;

    private ProgressBar() {
    }

    public static ProgressBar getTimerBasedProgressBarInSeconds(long total) {
        return getTimerBasedProgressBar(Duration.of(total, ChronoUnit.SECONDS), ChronoUnit.SECONDS);
    }

    public static ProgressBar getTimerBasedProgressBar(long total, ChronoUnit unit) {
        return getTimerBasedProgressBar(Duration.of(total, unit), unit);
    }

    public static ProgressBar getTimerBasedProgressBar(Duration duration, ChronoUnit unit) {
        Optional<ProgressBar> optional = pool.stream().filter(p -> p.finished).findFirst();
        ProgressBar p;
        if (optional.isPresent()) {
            p = optional.get();
        } else {
            p = new ProgressBar();
            pool.add(p);
        }
        p.duration = duration;
        p.unit = unit;
        p.finished = true;
        return p;
    }

    @Override
    public void run() {
        printTimeBaseProgressBar();
        finished = true;
    }

    public void start() {
        start(Console.LINE_BREAK);
    }

    public void start(Object append) {
        if (!finished) {
            throw new IllegalStateException("Thread for Progressbar is not finished yet.");
        }
        t = new Thread(this);
        curserPosition = Console.write(append);
        finished = false;
        t.start();
    }

    /**
     * DO NOT SYNCHRONIZE THIS FUNCTION.
     *
     */
    public void finish() {
        finished = true;
        try {
            t.join();
        } catch (InterruptedException ex) {
        }
        Console.writeLine();
    }

    public boolean isFinished() {
        return finished;
    }

    private void printTimeBaseProgressBar() {
        final long startTimeInMillis = System.currentTimeMillis();
        final long endTimeInMillis = startTimeInMillis + duration.toMillis();
        long currentTimeInMillis = startTimeInMillis;

        while (!finished && currentTimeInMillis < endTimeInMillis) {

            try {
                long millisToSleep = printTimeBasedProgress(unit, duration, startTimeInMillis, currentTimeInMillis);
                Thread.sleep(millisToSleep);
                currentTimeInMillis = System.currentTimeMillis();
            } catch (InterruptedException ex) {
            }
        }
        printTimeBasedProgress(unit, duration, startTimeInMillis, endTimeInMillis);
    }

    /**
     * returns the time to sleep in millis for the next time based progress
     *
     * @param unit
     * @param duration
     * @param startTimeInMillis
     * @param currentTimeInMillis
     * @return
     */
    private long printTimeBasedProgress(ChronoUnit unit, Duration duration, long startTimeInMillis, long currentTimeInMillis) {
        final long minThreadSleepInMillis = 50;
        long millisToSleep;
        switch (unit) {
            case SECONDS:
                long startTimeInSeconds = startTimeInMillis / 1000;
                long currentTimeInSeconds = currentTimeInMillis / 1000;
                printProgress(startTimeInMillis, duration.toSeconds(), currentTimeInSeconds - startTimeInSeconds, curserPosition);
                millisToSleep = unit.getDuration().toMillis();
                break;
            default:
                printProgress(startTimeInMillis, duration.toMillis(), currentTimeInMillis - startTimeInMillis, curserPosition);
                millisToSleep = unit.getDuration().toMillis() * minThreadSleepInMillis;
                break;
        }
        return millisToSleep;
    }

    public static synchronized void printProgress(final long start, final long total, final long current, final long cursorPos) {
        long eta = current == 0 ? 0
                : (total - current) * (System.currentTimeMillis() - start) / current;

        String etaHms = current == 0 ? "N/A"
                : String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        long curserPosConsole = Console.getCursorPosition();
        Console.CursorMovement move = Console.CursorMovement.NONE;
        int offset = 0;
        if (cursorPos < curserPosConsole) {
            offset = (int) (curserPosConsole - cursorPos);
            move = Console.CursorMovement.UP;
        } else if (cursorPos > curserPosConsole) {
            offset = (int) (cursorPos - curserPosConsole);
            move = Console.CursorMovement.DOWN;
        }
        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        double totalLog10Double = Math.log10(total);
        double currentLog10Double = Math.log10(current);

        int totalLog10Int = (int) totalLog10Double;
        int currentLog10Int = currentLog10Double == Double.NEGATIVE_INFINITY ? 0 : (int) currentLog10Double;
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies(totalLog10Int - currentLog10Int, " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms))
                .append(" ");

        Console.writeAndMoveCursor(offset, string.toString(), move);
    }

    public static void main(String[] args) {
        System.setProperty(MiscConstants.KEY_LOG_FILE_PATH, PathResolver.getJarFilePathOrWorkingDirectory().toString() + "/logs");
        Console.initLineReader(args);
        ProgressBar p1 = ProgressBar.getTimerBasedProgressBar(800, ChronoUnit.SECONDS);
        p1.start();
        ProgressBar p2 = ProgressBar.getTimerBasedProgressBar(1000 * 400, ChronoUnit.MILLIS);
        p2.start();

        long lt = System.currentTimeMillis();
        while (true) {
            try {
                String rl = Console.readLine();
                long ct = System.currentTimeMillis();
                if (ct > lt + 5000) {
                    Console.write("SOSO");
                    lt = ct;
                }

                if (rl.equals("exit")) {
                    throw new UserInterruptException("");
                } else if (rl.equals("stop")) {
                    p1.finish();
                    p2.finish();
                }
            } catch (UserInterruptException ex) {
                p1.finish();
                p2.finish();
                break;
            }
        }
    }
}
