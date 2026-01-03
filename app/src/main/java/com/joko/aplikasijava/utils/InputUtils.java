package com.joko.aplikasijava.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Utility class untuk input dari console
 */
public class InputUtils {
    
    private static final Scanner scanner = new Scanner(System.in);
    
    private InputUtils() {}
    
    /**
     * Baca input string
     */
    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * Baca input string dengan default value
     */
    public static String readString(String prompt, String defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
    
    /**
     * Baca input integer
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid. Masukkan angka bulat.");
            }
        }
    }
    
    /**
     * Baca input integer dengan range
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("Input harus antara %d dan %d.%n", min, max);
        }
    }
    
    /**
     * Baca input integer dengan default value
     */
    public static int readInt(String prompt, int defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid, menggunakan default: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Baca input long
     */
    public static long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid. Masukkan angka.");
            }
        }
    }
    
    /**
     * Baca input double
     */
    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid. Masukkan angka desimal.");
            }
        }
    }
    
    /**
     * Baca input BigDecimal
     */
    public static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid. Masukkan angka.");
            }
        }
    }
    
    /**
     * Baca input BigDecimal dengan default value
     */
    public static BigDecimal readBigDecimal(String prompt, BigDecimal defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid, menggunakan default: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Baca input boolean (y/n)
     */
    public static boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes") || input.equals("ya")) {
                return true;
            } else if (input.equals("n") || input.equals("no") || input.equals("tidak")) {
                return false;
            }
            System.out.println("Input tidak valid. Masukkan 'y' atau 'n'.");
        }
    }
    
    /**
     * Baca input tanggal (dd-MM-yyyy)
     */
    public static LocalDate readDate(String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        while (true) {
            System.out.print(prompt + " (dd-MM-yyyy): ");
            try {
                String input = scanner.nextLine().trim();
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Format tanggal tidak valid. Gunakan format dd-MM-yyyy.");
            }
        }
    }
    
    /**
     * Baca input tanggal dengan default hari ini
     */
    public static LocalDate readDate(String prompt, LocalDate defaultValue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        System.out.print(prompt + " [" + defaultValue.format(formatter) + "]: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(input, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Format tidak valid, menggunakan default.");
            return defaultValue;
        }
    }
    
    /**
     * Baca pilihan menu
     */
    public static int readMenu(String prompt, int maxOption) {
        return readInt(prompt, 0, maxOption);
    }
    
    /**
     * Pause dan tunggu ENTER
     */
    public static void pause() {
        System.out.print("\nTekan ENTER untuk melanjutkan...");
        scanner.nextLine();
    }
    
    /**
     * Pause dengan pesan custom
     */
    public static void pause(String message) {
        System.out.print(message);
        scanner.nextLine();
    }
    
    /**
     * Clear console (best effort)
     */
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Close scanner
     */
    public static void close() {
        scanner.close();
    }
}
