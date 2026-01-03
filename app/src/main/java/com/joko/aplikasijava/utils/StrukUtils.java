package com.joko.aplikasijava.utils;

import com.joko.aplikasijava.config.AppConfig;
import com.joko.aplikasijava.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Utility class untuk mencetak struk/receipt
 */
public class StrukUtils {
    
    private static final int LEBAR_STRUK = 48;
    private static final String LINE = "=".repeat(LEBAR_STRUK);
    private static final String DASH = "-".repeat(LEBAR_STRUK);
    
    private StrukUtils() {}
    
    /**
     * Cetak struk transaksi ke console
     */
    public static String cetakStruk(Transaksi transaksi) {
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("\n").append(LINE).append("\n");
        sb.append(center(AppConfig.APP_NAME)).append("\n");
        sb.append(center("Sistem Kasir Modern")).append("\n");
        sb.append(LINE).append("\n");
        
        // Info transaksi
        sb.append("No. Transaksi : ").append(transaksi.getNomorTransaksi()).append("\n");
        sb.append("Tanggal       : ").append(FormatUtils.formatDateTime(transaksi.getTanggalTransaksi())).append("\n");
        sb.append("Kasir         : ").append(transaksi.getKasir().getNamaLengkap()).append("\n");
        if (transaksi.getPelanggan() != null) {
            sb.append("Pelanggan     : ").append(transaksi.getPelanggan().getNama()).append("\n");
            if (transaksi.getPelanggan().getKodeMember() != null) {
                sb.append("Member        : ").append(transaksi.getPelanggan().getKodeMember()).append("\n");
            }
        }
        sb.append(DASH).append("\n");
        
        // Detail item
        List<DetailTransaksi> items = transaksi.getDetailTransaksiList();
        for (DetailTransaksi item : items) {
            String nama = FormatUtils.truncate(item.getNamaProduk(), 30);
            sb.append(nama).append("\n");
            
            String qty = String.format("  %d x %s", item.getQty(), FormatUtils.formatRupiah(item.getHargaSatuan()));
            String subtotal = FormatUtils.formatRupiah(item.getSubtotal());
            sb.append(formatLine(qty, subtotal)).append("\n");
            
            if (item.getDiskonPersen() != null && item.getDiskonPersen().compareTo(BigDecimal.ZERO) > 0) {
                sb.append(formatLine("  Diskon " + item.getDiskonPersen() + "%", "-")).append("\n");
            }
        }
        sb.append(DASH).append("\n");
        
        // Summary
        sb.append(formatLine("Subtotal", FormatUtils.formatRupiah(transaksi.getSubtotal()))).append("\n");
        
        if (transaksi.getDiskonPersen() != null && transaksi.getDiskonPersen().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(formatLine("Diskon (" + transaksi.getDiskonPersen() + "%)", 
                    "-" + FormatUtils.formatRupiah(transaksi.getSubtotal().multiply(transaksi.getDiskonPersen()).divide(new BigDecimal("100"))))).append("\n");
        }
        
        if (transaksi.getDiskonNominal() != null && transaksi.getDiskonNominal().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(formatLine("Diskon", "-" + FormatUtils.formatRupiah(transaksi.getDiskonNominal()))).append("\n");
        }
        
        if (transaksi.getPpnNominal() != null && transaksi.getPpnNominal().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(formatLine("PPN (" + transaksi.getPpnPersen() + "%)", FormatUtils.formatRupiah(transaksi.getPpnNominal()))).append("\n");
        }
        
        sb.append(DASH).append("\n");
        sb.append(formatLineBold("TOTAL", FormatUtils.formatRupiah(transaksi.getGrandTotal()))).append("\n");
        sb.append(DASH).append("\n");
        
        // Pembayaran
        for (Pembayaran pembayaran : transaksi.getPembayaranList()) {
            sb.append(formatLine(pembayaran.getMetodePembayaran(), FormatUtils.formatRupiah(pembayaran.getJumlah()))).append("\n");
        }
        sb.append(formatLine("Kembalian", FormatUtils.formatRupiah(transaksi.getKembalian()))).append("\n");
        
        // Poin
        if (transaksi.getPelanggan() != null && transaksi.getPoinDidapat() > 0) {
            sb.append(DASH).append("\n");
            sb.append(formatLine("Poin Didapat", "+" + transaksi.getPoinDidapat())).append("\n");
            sb.append(formatLine("Total Poin", String.valueOf(transaksi.getPelanggan().getPoin()))).append("\n");
        }
        
        // Footer
        sb.append(LINE).append("\n");
        sb.append(center("Terima Kasih")).append("\n");
        sb.append(center("Atas Kunjungan Anda")).append("\n");
        sb.append(center("Barang yang sudah dibeli")).append("\n");
        sb.append(center("tidak dapat dikembalikan")).append("\n");
        sb.append(LINE).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Cetak struk ringkas
     */
    public static String cetakStrukRingkas(Transaksi transaksi) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n").append(DASH).append("\n");
        sb.append("TRX: ").append(transaksi.getNomorTransaksi()).append("\n");
        sb.append("Tgl: ").append(FormatUtils.formatDateTime(transaksi.getTanggalTransaksi())).append("\n");
        sb.append(DASH).append("\n");
        sb.append(String.format("Items: %d | Qty: %d\n", transaksi.getTotalItem(), transaksi.getTotalQty()));
        sb.append(formatLine("TOTAL", FormatUtils.formatRupiah(transaksi.getGrandTotal()))).append("\n");
        sb.append(formatLine("BAYAR", FormatUtils.formatRupiah(transaksi.getTotalBayar()))).append("\n");
        sb.append(formatLine("KEMBALI", FormatUtils.formatRupiah(transaksi.getKembalian()))).append("\n");
        sb.append(DASH).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Format baris dengan label di kiri dan value di kanan
     */
    private static String formatLine(String left, String right) {
        int spaces = LEBAR_STRUK - left.length() - right.length();
        if (spaces < 1) spaces = 1;
        return left + " ".repeat(spaces) + right;
    }
    
    /**
     * Format baris bold (pakai asterisk)
     */
    private static String formatLineBold(String left, String right) {
        String formatted = "** " + left;
        int spaces = LEBAR_STRUK - formatted.length() - right.length() - 3;
        if (spaces < 1) spaces = 1;
        return formatted + " ".repeat(spaces) + right + " **";
    }
    
    /**
     * Center text
     */
    private static String center(String text) {
        if (text.length() >= LEBAR_STRUK) return text;
        int padding = (LEBAR_STRUK - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
    
    /**
     * Cetak laporan penjualan harian
     */
    public static String cetakLaporanHarian(LocalDateTime tanggal, List<Transaksi> transaksiList, 
            BigDecimal totalPenjualan, int jumlahTransaksi) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n").append(LINE).append("\n");
        sb.append(center("LAPORAN PENJUALAN HARIAN")).append("\n");
        sb.append(center(AppConfig.APP_NAME)).append("\n");
        sb.append(LINE).append("\n");
        
        sb.append("Tanggal: ").append(FormatUtils.formatTanggal(tanggal.toLocalDate())).append("\n");
        sb.append(DASH).append("\n");
        
        sb.append(formatLine("Jumlah Transaksi", String.valueOf(jumlahTransaksi))).append("\n");
        sb.append(formatLine("Total Penjualan", FormatUtils.formatRupiah(totalPenjualan))).append("\n");
        
        sb.append(LINE).append("\n");
        
        return sb.toString();
    }
}
