package com.joko.aplikasijava.services;

import com.joko.aplikasijava.models.*;
import com.joko.aplikasijava.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service untuk laporan dan analisis
 */
public class LaporanService {
    
    private static final Logger logger = LoggerFactory.getLogger(LaporanService.class);
    private final TransaksiRepository transaksiRepository;
    private final ProdukRepository produkRepository;
    private final PelangganRepository pelangganRepository;
    
    public LaporanService() {
        this.transaksiRepository = new TransaksiRepository();
        this.produkRepository = new ProdukRepository();
        this.pelangganRepository = new PelangganRepository();
    }
    
    // ==================== LAPORAN PENJUALAN ====================
    
    /**
     * Ringkasan penjualan hari ini
     */
    public RingkasanPenjualan getRingkasanHariIni() {
        return getRingkasanByTanggal(LocalDate.now());
    }
    
    /**
     * Ringkasan penjualan per tanggal
     */
    public RingkasanPenjualan getRingkasanByTanggal(LocalDate tanggal) {
        List<Transaksi> transaksiList = transaksiRepository.findByTanggal(tanggal);
        return hitungRingkasan(transaksiList, tanggal, tanggal);
    }
    
    /**
     * Ringkasan penjualan per periode
     */
    public RingkasanPenjualan getRingkasanByPeriode(LocalDate startDate, LocalDate endDate) {
        List<Transaksi> transaksiList = transaksiRepository.findByDateRange(startDate, endDate);
        return hitungRingkasan(transaksiList, startDate, endDate);
    }
    
    /**
     * Hitung ringkasan dari list transaksi
     */
    private RingkasanPenjualan hitungRingkasan(List<Transaksi> transaksiList, LocalDate startDate, LocalDate endDate) {
        RingkasanPenjualan ringkasan = new RingkasanPenjualan();
        ringkasan.setTanggalMulai(startDate);
        ringkasan.setTanggalSelesai(endDate);
        
        BigDecimal totalPenjualan = BigDecimal.ZERO;
        BigDecimal totalDiskon = BigDecimal.ZERO;
        BigDecimal totalPPN = BigDecimal.ZERO;
        BigDecimal tunai = BigDecimal.ZERO;
        BigDecimal nonTunai = BigDecimal.ZERO;
        int jumlahTransaksi = 0;
        int jumlahItem = 0;
        int transaksiMember = 0;
        int transaksiUmum = 0;
        
        for (Transaksi trx : transaksiList) {
            if ("SELESAI".equals(trx.getStatus())) {
                totalPenjualan = totalPenjualan.add(trx.getGrandTotal());
                totalDiskon = totalDiskon.add(trx.getDiskonNominal());
                totalPPN = totalPPN.add(trx.getPpnNominal());
                jumlahTransaksi++;
                jumlahItem += trx.getTotalQty();
                
                if (trx.getPelanggan() != null && trx.getPelanggan().isMember()) {
                    transaksiMember++;
                } else {
                    transaksiUmum++;
                }
                
                // Hitung per metode pembayaran
                for (Pembayaran pembayaran : trx.getPembayaranList()) {
                    if ("TUNAI".equals(pembayaran.getMetodePembayaran())) {
                        tunai = tunai.add(pembayaran.getJumlah());
                    } else {
                        nonTunai = nonTunai.add(pembayaran.getJumlah());
                    }
                }
            }
        }
        
        ringkasan.setTotalPenjualan(totalPenjualan);
        ringkasan.setTotalDiskon(totalDiskon);
        ringkasan.setTotalPPN(totalPPN);
        ringkasan.setJumlahTransaksi(jumlahTransaksi);
        ringkasan.setJumlahItem(jumlahItem);
        ringkasan.setTunai(tunai);
        ringkasan.setNonTunai(nonTunai);
        ringkasan.setTransaksiMember(transaksiMember);
        ringkasan.setTransaksiUmum(transaksiUmum);
        
        // Rata-rata per transaksi
        if (jumlahTransaksi > 0) {
            ringkasan.setRataRataTransaksi(totalPenjualan.divide(new BigDecimal(jumlahTransaksi), 2, java.math.RoundingMode.HALF_UP));
        }
        
        return ringkasan;
    }
    
    // ==================== LAPORAN PRODUK ====================
    
    /**
     * Produk terlaris
     */
    public List<Produk> getProdukTerlaris(int limit) {
        return produkRepository.findBestSellers(limit);
    }
    
    /**
     * Produk stok rendah
     */
    public List<Produk> getProdukStokRendah() {
        return produkRepository.findLowStock();
    }
    
    /**
     * Produk habis stok
     */
    public List<Produk> getProdukHabisStok() {
        return produkRepository.findOutOfStock();
    }
    
    /**
     * Total nilai stok
     */
    public BigDecimal getTotalNilaiStok() {
        return produkRepository.getTotalStockValue();
    }
    
    /**
     * Jumlah produk aktif
     */
    public long getJumlahProduk() {
        return produkRepository.countActive();
    }
    
    // ==================== LAPORAN PELANGGAN ====================
    
    /**
     * Pelanggan top spender
     */
    public List<Pelanggan> getTopSpenders(int limit) {
        return pelangganRepository.findTopByTotalBelanja(limit);
    }
    
    /**
     * Pelanggan dengan poin terbanyak
     */
    public List<Pelanggan> getTopByPoin(int limit) {
        return pelangganRepository.findTopByPoin(limit);
    }
    
    /**
     * Jumlah member
     */
    public long getJumlahMember() {
        return pelangganRepository.countMembers();
    }
    
    // ==================== DASHBOARD ====================
    
    /**
     * Data untuk dashboard
     */
    public DashboardData getDashboardData() {
        DashboardData data = new DashboardData();
        
        // Penjualan hari ini
        data.setPenjualanHariIni(transaksiRepository.getTotalPenjualanHariIni());
        data.setTransaksiHariIni(transaksiRepository.countTransaksiHariIni());
        
        // Produk
        data.setTotalProduk(produkRepository.countActive());
        data.setProdukStokRendah(produkRepository.findLowStock().size());
        data.setNilaiStok(produkRepository.getTotalStockValue());
        
        // Member
        data.setTotalMember(pelangganRepository.countMembers());
        
        // Transaksi terakhir
        data.setTransaksiTerakhir(transaksiRepository.findRecent(5));
        
        // Produk terlaris
        data.setProdukTerlaris(produkRepository.findBestSellers(5));
        
        return data;
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * DTO untuk ringkasan penjualan
     */
    public static class RingkasanPenjualan {
        private LocalDate tanggalMulai;
        private LocalDate tanggalSelesai;
        private BigDecimal totalPenjualan = BigDecimal.ZERO;
        private BigDecimal totalDiskon = BigDecimal.ZERO;
        private BigDecimal totalPPN = BigDecimal.ZERO;
        private int jumlahTransaksi;
        private int jumlahItem;
        private BigDecimal tunai = BigDecimal.ZERO;
        private BigDecimal nonTunai = BigDecimal.ZERO;
        private int transaksiMember;
        private int transaksiUmum;
        private BigDecimal rataRataTransaksi = BigDecimal.ZERO;
        
        // Getters and Setters
        public LocalDate getTanggalMulai() { return tanggalMulai; }
        public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }
        
        public LocalDate getTanggalSelesai() { return tanggalSelesai; }
        public void setTanggalSelesai(LocalDate tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }
        
        public BigDecimal getTotalPenjualan() { return totalPenjualan; }
        public void setTotalPenjualan(BigDecimal totalPenjualan) { this.totalPenjualan = totalPenjualan; }
        
        public BigDecimal getTotalDiskon() { return totalDiskon; }
        public void setTotalDiskon(BigDecimal totalDiskon) { this.totalDiskon = totalDiskon; }
        
        public BigDecimal getTotalPPN() { return totalPPN; }
        public void setTotalPPN(BigDecimal totalPPN) { this.totalPPN = totalPPN; }
        
        public int getJumlahTransaksi() { return jumlahTransaksi; }
        public void setJumlahTransaksi(int jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }
        
        public int getJumlahItem() { return jumlahItem; }
        public void setJumlahItem(int jumlahItem) { this.jumlahItem = jumlahItem; }
        
        public BigDecimal getTunai() { return tunai; }
        public void setTunai(BigDecimal tunai) { this.tunai = tunai; }
        
        public BigDecimal getNonTunai() { return nonTunai; }
        public void setNonTunai(BigDecimal nonTunai) { this.nonTunai = nonTunai; }
        
        public int getTransaksiMember() { return transaksiMember; }
        public void setTransaksiMember(int transaksiMember) { this.transaksiMember = transaksiMember; }
        
        public int getTransaksiUmum() { return transaksiUmum; }
        public void setTransaksiUmum(int transaksiUmum) { this.transaksiUmum = transaksiUmum; }
        
        public BigDecimal getRataRataTransaksi() { return rataRataTransaksi; }
        public void setRataRataTransaksi(BigDecimal rataRataTransaksi) { this.rataRataTransaksi = rataRataTransaksi; }
    }
    
    /**
     * DTO untuk data dashboard
     */
    public static class DashboardData {
        private BigDecimal penjualanHariIni = BigDecimal.ZERO;
        private long transaksiHariIni;
        private long totalProduk;
        private int produkStokRendah;
        private BigDecimal nilaiStok = BigDecimal.ZERO;
        private long totalMember;
        private List<Transaksi> transaksiTerakhir;
        private List<Produk> produkTerlaris;
        
        // Getters and Setters
        public BigDecimal getPenjualanHariIni() { return penjualanHariIni; }
        public void setPenjualanHariIni(BigDecimal penjualanHariIni) { this.penjualanHariIni = penjualanHariIni; }
        
        public long getTransaksiHariIni() { return transaksiHariIni; }
        public void setTransaksiHariIni(long transaksiHariIni) { this.transaksiHariIni = transaksiHariIni; }
        
        public long getTotalProduk() { return totalProduk; }
        public void setTotalProduk(long totalProduk) { this.totalProduk = totalProduk; }
        
        public int getProdukStokRendah() { return produkStokRendah; }
        public void setProdukStokRendah(int produkStokRendah) { this.produkStokRendah = produkStokRendah; }
        
        public BigDecimal getNilaiStok() { return nilaiStok; }
        public void setNilaiStok(BigDecimal nilaiStok) { this.nilaiStok = nilaiStok; }
        
        public long getTotalMember() { return totalMember; }
        public void setTotalMember(long totalMember) { this.totalMember = totalMember; }
        
        public List<Transaksi> getTransaksiTerakhir() { return transaksiTerakhir; }
        public void setTransaksiTerakhir(List<Transaksi> transaksiTerakhir) { this.transaksiTerakhir = transaksiTerakhir; }
        
        public List<Produk> getProdukTerlaris() { return produkTerlaris; }
        public void setProdukTerlaris(List<Produk> produkTerlaris) { this.produkTerlaris = produkTerlaris; }
    }
}
