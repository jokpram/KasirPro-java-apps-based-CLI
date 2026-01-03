package com.joko.aplikasijava.config;

/**
 * Konfigurasi aplikasi sistem kasir
 * Berisi konstanta dan pengaturan global
 */
public class AppConfig {
    
    // Informasi Aplikasi
    public static final String APP_NAME = "KASIR PRO";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_AUTHOR = "Joko";
    
    // Format Mata Uang
    public static final String CURRENCY_SYMBOL = "Rp";
    public static final String CURRENCY_FORMAT = "#,##0.00";
    
    // Format Tanggal
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String DATETIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    
    // Pengaturan Transaksi
    public static final double TAX_RATE = 0.11; // PPN 11%
    public static final double SERVICE_CHARGE = 0.0; // Service charge 0%
    public static final int MAX_ITEM_PER_TRANSACTION = 100;
    
    // Pengaturan Stok
    public static final int LOW_STOCK_THRESHOLD = 10;
    public static final int CRITICAL_STOCK_THRESHOLD = 5;
    
    // Pengaturan Diskon
    public static final double MAX_DISCOUNT_PERCENTAGE = 50.0;
    public static final double MEMBER_DISCOUNT = 5.0; // Diskon member 5%
    
    // Pengaturan Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Pengaturan Keamanan
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    
    // Format Nomor Transaksi
    public static final String TRANSACTION_PREFIX = "TRX";
    public static final String INVOICE_PREFIX = "INV";
    public static final String PURCHASE_PREFIX = "PO";
    
    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_KASIR = "KASIR";
    public static final String ROLE_SUPERVISOR = "SUPERVISOR";
    
    // Payment Methods
    public static final String PAYMENT_CASH = "TUNAI";
    public static final String PAYMENT_DEBIT = "DEBIT";
    public static final String PAYMENT_CREDIT = "KREDIT";
    public static final String PAYMENT_QRIS = "QRIS";
    public static final String PAYMENT_TRANSFER = "TRANSFER";
    
    // Transaction Status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "SELESAI";
    public static final String STATUS_CANCELLED = "DIBATALKAN";
    public static final String STATUS_REFUNDED = "DIKEMBALIKAN";
    
    private AppConfig() {
        // Private constructor to prevent instantiation
    }
}
