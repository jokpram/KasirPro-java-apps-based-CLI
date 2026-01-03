package com.joko.aplikasijava.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class untuk formatting
 */
public class FormatUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    
    private FormatUtils() {}
    
    /**
     * Format tanggal ke string dd-MM-yyyy
     */
    public static String formatTanggal(LocalDate date) {
        if (date == null) return "-";
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Format datetime ke string dd-MM-yyyy HH:mm:ss
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    /**
     * Format waktu ke string HH:mm:ss
     */
    public static String formatWaktu(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(TIME_FORMATTER);
    }
    
    /**
     * Format angka ke mata uang Rupiah
     */
    public static String formatRupiah(BigDecimal amount) {
        if (amount == null) return "Rp 0";
        return CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Format angka ke mata uang Rupiah
     */
    public static String formatRupiah(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Format BigDecimal ke string dengan 2 desimal
     */
    public static String formatDecimal(BigDecimal value) {
        if (value == null) return "0.00";
        return DECIMAL_FORMAT.format(value);
    }
    
    /**
     * Format angka dengan separator ribuan
     */
    public static String formatNumber(int number) {
        return String.format("%,d", number);
    }
    
    /**
     * Format angka dengan separator ribuan
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
    
    /**
     * Parse string ke BigDecimal
     */
    public static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) return BigDecimal.ZERO;
        try {
            // Remove currency symbol and thousand separator
            String cleaned = value.replaceAll("[^0-9.,\\-]", "").replace(",", "");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Pad string ke kiri
     */
    public static String padLeft(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder();
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }
    
    /**
     * Pad string ke kanan
     */
    public static String padRight(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        StringBuilder sb = new StringBuilder(str);
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }
    
    /**
     * Truncate string
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Generate separator line
     */
    public static String separator(int length, char c) {
        return String.valueOf(c).repeat(length);
    }
    
    /**
     * Center text dalam width tertentu
     */
    public static String center(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(padding);
    }
}
