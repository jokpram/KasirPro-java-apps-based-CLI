/*
 * Test untuk Sistem Kasir KASIR PRO
 */
package com.joko.aplikasijava;

import com.joko.aplikasijava.config.AppConfig;
import com.joko.aplikasijava.models.*;
import com.joko.aplikasijava.utils.FormatUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unit tests untuk aplikasi sistem kasir
 */
class AppTest {
    
    @Test
    void testAppConfig() {
        // Test konstanta aplikasi
        assertNotNull(AppConfig.APP_NAME);
        assertNotNull(AppConfig.APP_VERSION);
        assertEquals("KASIR PRO", AppConfig.APP_NAME);
        assertEquals(0.11, AppConfig.TAX_RATE);
    }
    
    @Test
    void testFormatRupiah() {
        // Test formatting mata uang
        BigDecimal amount = new BigDecimal("10000");
        String formatted = FormatUtils.formatRupiah(amount);
        assertNotNull(formatted);
        assertTrue(formatted.contains("10"));
    }
    
    @Test
    void testFormatTanggal() {
        // Test formatting tanggal
        LocalDate date = LocalDate.of(2024, 1, 15);
        String formatted = FormatUtils.formatTanggal(date);
        assertEquals("15-01-2024", formatted);
    }
    
    @Test
    void testProdukEntity() {
        // Test pembuatan entity Produk
        Produk produk = new Produk("PRD001", "Test Produk", 
                new BigDecimal("5000"), new BigDecimal("7500"), 100, "PCS");
        
        assertEquals("PRD001", produk.getKode());
        assertEquals("Test Produk", produk.getNama());
        assertEquals(new BigDecimal("5000"), produk.getHargaBeli());
        assertEquals(new BigDecimal("7500"), produk.getHargaJual());
        assertEquals(100, produk.getStok());
        assertEquals("PCS", produk.getSatuan());
    }
    
    @Test
    void testProdukMargin() {
        // Test kalkulasi margin produk
        Produk produk = new Produk("PRD002", "Test", 
                new BigDecimal("10000"), new BigDecimal("15000"), 50, "PCS");
        
        BigDecimal margin = produk.getMargin();
        assertEquals(new BigDecimal("5000"), margin);
    }
    
    @Test
    void testProdukLowStock() {
        // Test deteksi stok rendah
        Produk produk = new Produk("PRD003", "Test", 
                new BigDecimal("1000"), new BigDecimal("2000"), 5, "PCS");
        produk.setStokMinimum(10);
        
        assertTrue(produk.isLowStock());
    }
    
    @Test
    void testProdukOutOfStock() {
        // Test deteksi stok habis
        Produk produk = new Produk("PRD004", "Test", 
                new BigDecimal("1000"), new BigDecimal("2000"), 0, "PCS");
        
        assertTrue(produk.isOutOfStock());
    }
    
    @Test
    void testUserEntity() {
        // Test pembuatan entity User
        User user = new User("testuser", "hashedpassword", "Test User", "KASIR");
        
        assertEquals("testuser", user.getUsername());
        assertEquals("Test User", user.getNamaLengkap());
        assertEquals("KASIR", user.getRole());
        assertTrue(user.getAktif());
        assertFalse(user.getTerkunci());
    }
    
    @Test
    void testKategoriEntity() {
        // Test pembuatan entity Kategori
        Kategori kategori = new Kategori("KAT001", "Makanan", "Kategori makanan");
        
        assertEquals("KAT001", kategori.getKode());
        assertEquals("Makanan", kategori.getNama());
        assertEquals("Kategori makanan", kategori.getDeskripsi());
        assertTrue(kategori.getAktif());
    }
    
    @Test
    void testPelangganMembership() {
        // Test sistem member pelanggan
        Pelanggan pelanggan = new Pelanggan("MBR001", "John Doe", "081234567890");
        
        assertTrue(pelanggan.isMember());
        assertEquals("REGULAR", pelanggan.getTipeMember());
        assertEquals(0, pelanggan.getPoin());
    }
    
    @Test
    void testPelangganPoin() {
        // Test penambahan poin
        Pelanggan pelanggan = new Pelanggan("MBR002", "Jane Doe", "082345678901");
        pelanggan.tambahPoin(100);
        
        assertEquals(100, pelanggan.getPoin());
        
        // Test kurangi poin
        boolean result = pelanggan.kurangiPoin(50);
        assertTrue(result);
        assertEquals(50, pelanggan.getPoin());
        
        // Test kurangi poin lebih dari yang ada
        result = pelanggan.kurangiPoin(100);
        assertFalse(result);
        assertEquals(50, pelanggan.getPoin()); // Tetap 50
    }
    
    @Test
    void testDetailTransaksi() {
        // Test pembuatan detail transaksi
        Produk produk = new Produk("PRD005", "Test", 
                new BigDecimal("5000"), new BigDecimal("7500"), 100, "PCS");
        
        DetailTransaksi detail = new DetailTransaksi(produk, 3);
        
        assertEquals(3, detail.getQty());
        assertEquals(new BigDecimal("7500"), detail.getHargaSatuan());
        assertEquals(new BigDecimal("22500"), detail.getSubtotal()); // 3 x 7500
    }
    
    @Test
    void testDetailTransaksiWithDiskon() {
        // Test detail transaksi dengan diskon
        Produk produk = new Produk("PRD006", "Test", 
                new BigDecimal("5000"), new BigDecimal("10000"), 100, "PCS");
        produk.setDiskonPersen(new BigDecimal("10"));
        
        DetailTransaksi detail = new DetailTransaksi(produk, 2);
        
        // Subtotal = 2 x 10000 = 20000
        // Diskon 10% = 2000
        // Total = 18000
        // Use compareTo for BigDecimal comparison to avoid scale issues
        assertEquals(0, new BigDecimal("18000").compareTo(detail.getSubtotal()));
    }
    
    @Test
    void testPembayaran() {
        // Test pembuatan pembayaran
        Pembayaran pembayaran = new Pembayaran("TUNAI", new BigDecimal("50000"));
        
        assertEquals("TUNAI", pembayaran.getMetodePembayaran());
        assertEquals(new BigDecimal("50000"), pembayaran.getJumlah());
        assertTrue(pembayaran.isCash());
        assertFalse(pembayaran.isCard());
    }
    
    @Test
    void testTruncateString() {
        // Test truncate string
        String longText = "Ini adalah text yang sangat panjang sekali";
        String truncated = FormatUtils.truncate(longText, 20);
        
        assertEquals(20, truncated.length());
        assertTrue(truncated.endsWith("..."));
    }
    
    @Test
    void testPadLeft() {
        // Test pad left
        String result = FormatUtils.padLeft("123", 6, '0');
        assertEquals("000123", result);
    }
    
    @Test
    void testPadRight() {
        // Test pad right
        String result = FormatUtils.padRight("ABC", 6, ' ');
        assertEquals("ABC   ", result);
    }
}
