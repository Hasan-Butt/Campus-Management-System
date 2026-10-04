package com.fast.campus;

/**
 * Application entry point for the Campus Management System.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
