package com.fast.campus.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Shared application logger.
 * Writes structured log entries to logs/campus.log using the format:
 * [YYYY-MM-DD HH:mm:ss] [LEVEL] [Actor] Event
 *
 * <p>All modules (Hasan, Kabeer, Saim) must use this logger to ensure
 * a consistent log format across the system.</p>
 */
public class Logger {

    private static final String LOG_FILE = "logs/campus.log";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Private constructor — utility class, no instantiation.
    private Logger() {}

    public static void info(String actor, String event) {
        log("INFO", actor, event);
    }

    public static void warn(String actor, String event) {
        log("WARN", actor, event);
    }

    public static void error(String actor, String event) {
        log("ERROR", actor, event);
    }

    private static void log(String level, String actor, String event) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String entry = String.format("[%s] [%s] [%s] %s", timestamp, level, actor, event);
        System.out.println(entry);
        writeToFile(entry);
    }

    private static void writeToFile(String entry) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(entry);
        } catch (IOException e) {
            System.err.println("Logger: failed to write to log file — " + e.getMessage());
        }
    }
}
