package com.fast.campus.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for simple pipe-delimited flat-file persistence.
 *
 * <p>Data format: one record per line, fields separated by '|'
 * Example: COURSE|CS101|Intro to SE|3</p>
 */
public class FileManager {

    // Private constructor — utility class.
    private FileManager() {}

    /**
     * Reads all lines from the given file path.
     *
     * @param filePath relative or absolute path to the data file
     * @return list of non-empty, non-comment lines
     */
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            Logger.error("FileManager", "Failed to read file: " + filePath + " — " + e.getMessage());
        }
        return lines;
    }

    /**
     * Appends a single line to the given file.
     *
     * @param filePath relative or absolute path to the data file
     * @param line     the line to append
     */
    public static void appendLine(String filePath, String line) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
            pw.println(line);
        } catch (IOException e) {
            Logger.error("FileManager", "Failed to write to file: " + filePath + " — " + e.getMessage());
        }
    }

    /**
     * Overwrites the file with the provided list of lines.
     *
     * @param filePath relative or absolute path to the data file
     * @param lines    the full content to write
     */
    public static void writeAllLines(String filePath, List<String> lines) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, false))) {
            for (String line : lines) {
                pw.println(line);
            }
        } catch (IOException e) {
            Logger.error("FileManager", "Failed to overwrite file: " + filePath + " — " + e.getMessage());
        }
    }
}
