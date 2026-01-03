package com.joko.aplikasijava.services;

import com.joko.aplikasijava.config.AppConfig;
import com.joko.aplikasijava.models.*;
import com.joko.aplikasijava.repositories.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk mengelola transaksi penjualan
 * Core service dari sistem kasir
 */
public class TransaksiService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransaksiService.class);
    private final TransaksiRepository transaksiRepository;
    private final ProdukRepository produkRepository;
    private final PelangganRepository pelangganRepository;
    private final StokHistoryRepository stokHistoryRepository;
    
    // Transaksi saat ini (keranjang belanja)
    private Transaksi transaksiAktif;
    private final List<DetailTransaksi> keranjang = new ArrayList<>();
    
    public TransaksiService() {
        this.transaksiRepository = new TransaksiRepository();
        this.produkRepository = new ProdukRepository();
        this.pelangganRepository = new PelangganRepository();
        this.stokHistoryRepository = new StokHistoryRepository();
    }
    
    // ==================== KERANJANG ====================
    
    /**
     * Mulai transaksi baru
     */
    public Transaksi mulaiTransaksiBaru(User kasir) {
        if (transaksiAktif != null && !keranjang.isEmpty()) {
            logger.warn("Ada transaksi yang belum selesai");
        }
        
        String nomorTransaksi = transaksiRepository.generateNomorTransaksi();
        transaksiAktif = new Transaksi(nomorTransaksi, kasir);
        keranjang.clear();
        
        logger.info("Transaksi baru dimulai: {}", nomorTransaksi);
        return transaksiAktif;
    }
    
    /**
     * Tambah item ke keranjang
     */
    public DetailTransaksi tambahKeKeranjang(String kodeOrBarcode, int qty) {
        if (transaksiAktif == null) {
            throw new IllegalStateException("Belum ada transaksi aktif. Mulai transaksi baru terlebih dahulu.");
        }
        
        // Cari produk
        Optional<Produk> produkOpt = produkRepository.findByKode(kodeOrBarcode);
        if (produkOpt.isEmpty()) {
            produkOpt = produkRepository.findByBarcode(kodeOrBarcode);
        }
        
        if (produkOpt.isEmpty()) {
            throw new IllegalArgumentException("Produk tidak ditemukan: " + kodeOrBarcode);
        }
        
        Produk produk = produkOpt.get();
        
        // Cek stok
        int qtyDiKeranjang = getQtyDiKeranjang(produk.getId());
        if (produk.getStok() < (qtyDiKeranjang + qty)) {
            throw new IllegalStateException("Stok tidak mencukupi. Tersedia: " + produk.getStok() + ", Di keranjang: " + qtyDiKeranjang);
        }
        
        // Cek apakah produk sudah ada di keranjang
        for (DetailTransaksi detail : keranjang) {
            if (detail.getProduk().getId().equals(produk.getId())) {
                detail.setQty(detail.getQty() + qty);
                detail.calculateSubtotal();
                hitungUlangTransaksi();
                return detail;
            }
        }
        
        // Tambah item baru
        DetailTransaksi detail = new DetailTransaksi(produk, qty);
        keranjang.add(detail);
        hitungUlangTransaksi();
        
        logger.info("Item ditambahkan ke keranjang: {} x{}", produk.getNama(), qty);
        return detail;
    }
    
    /**
     * Tambah item ke keranjang dengan produk
     */
    public DetailTransaksi tambahKeKeranjang(Produk produk, int qty) {
        return tambahKeKeranjang(produk.getKode(), qty);
    }
    
    /**
     * Update qty item di keranjang
     */
    public void updateQtyKeranjang(int index, int qty) {
        if (index < 0 || index >= keranjang.size()) {
            throw new IndexOutOfBoundsException("Index tidak valid");
        }
        
        DetailTransaksi detail = keranjang.get(index);
        Produk produk = detail.getProduk();
        
        // Refresh produk untuk cek stok terbaru
        Optional<Produk> produkOpt = produkRepository.findById(produk.getId());
        if (produkOpt.isPresent() && produkOpt.get().getStok() < qty) {
            throw new IllegalStateException("Stok tidak mencukupi. Tersedia: " + produkOpt.get().getStok());
        }
        
        if (qty <= 0) {
            hapusDariKeranjang(index);
        } else {
            detail.setQty(qty);
            detail.calculateSubtotal();
            hitungUlangTransaksi();
        }
    }
    
    /**
     * Hapus item dari keranjang
     */
    public void hapusDariKeranjang(int index) {
        if (index < 0 || index >= keranjang.size()) {
            throw new IndexOutOfBoundsException("Index tidak valid");
        }
        
        DetailTransaksi removed = keranjang.remove(index);
        hitungUlangTransaksi();
        logger.info("Item dihapus dari keranjang: {}", removed.getNamaProduk());
    }
    
    /**
     * Kosongkan keranjang
     */
    public void kosongkanKeranjang() {
        keranjang.clear();
        if (transaksiAktif != null) {
            transaksiAktif.setSubtotal(BigDecimal.ZERO);
            transaksiAktif.setGrandTotal(BigDecimal.ZERO);
            transaksiAktif.setTotalItem(0);
            transaksiAktif.setTotalQty(0);
        }
        logger.info("Keranjang dikosongkan");
    }
    
    /**
     * Dapatkan item di keranjang
     */
    public List<DetailTransaksi> getKeranjang() {
        return new ArrayList<>(keranjang);
    }
    
    /**
     * Dapatkan jumlah item di keranjang
     */
    public int getJumlahItemKeranjang() {
        return keranjang.size();
    }
    
    /**
     * Dapatkan qty produk di keranjang
     */
    private int getQtyDiKeranjang(Long produkId) {
        return keranjang.stream()
                .filter(d -> d.getProduk().getId().equals(produkId))
                .mapToInt(DetailTransaksi::getQty)
                .sum();
    }
    
    /**
     * Hitung ulang transaksi
     */
    private void hitungUlangTransaksi() {
        if (transaksiAktif == null) return;
        
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItem = 0;
        int totalQty = 0;
        
        for (DetailTransaksi detail : keranjang) {
            subtotal = subtotal.add(detail.getSubtotal());
            totalItem++;
            totalQty += detail.getQty();
        }
        
        transaksiAktif.setSubtotal(subtotal);
        transaksiAktif.setTotalItem(totalItem);
        transaksiAktif.setTotalQty(totalQty);
        transaksiAktif.recalculate();
    }
    
    // ==================== DISKON & PELANGGAN ====================
    
    /**
     * Set pelanggan untuk transaksi
     */
    public void setPelanggan(Long pelangganId) {
        if (transaksiAktif == null) {
            throw new IllegalStateException("Belum ada transaksi aktif");
        }
        
        Optional<Pelanggan> pelangganOpt = pelangganRepository.findById(pelangganId);
        if (pelangganOpt.isPresent()) {
            Pelanggan pelanggan = pelangganOpt.get();
            transaksiAktif.setPelanggan(pelanggan);
            
            // Apply diskon member jika ada
            if (pelanggan.getDiskonMember() != null && pelanggan.getDiskonMember().compareTo(BigDecimal.ZERO) > 0) {
                transaksiAktif.setDiskonPersen(pelanggan.getDiskonMember());
                hitungUlangTransaksi();
            }
            
            logger.info("Pelanggan diset: {}", pelanggan.getNama());
        }
    }
    
    /**
     * Set pelanggan berdasarkan kode member atau no telepon
     */
    public void setPelanggan(String kodeOrTelepon) {
        Optional<Pelanggan> pelangganOpt = pelangganRepository.findByKodeMember(kodeOrTelepon);
        if (pelangganOpt.isEmpty()) {
            pelangganOpt = pelangganRepository.findByNoTelepon(kodeOrTelepon);
        }
        
        if (pelangganOpt.isPresent()) {
            setPelanggan(pelangganOpt.get().getId());
        } else {
            throw new IllegalArgumentException("Pelanggan tidak ditemukan: " + kodeOrTelepon);
        }
    }
    
    /**
     * Set diskon untuk transaksi
     */
    public void setDiskon(BigDecimal diskonPersen, BigDecimal diskonNominal) {
        if (transaksiAktif == null) {
            throw new IllegalStateException("Belum ada transaksi aktif");
        }
        
        if (diskonPersen != null && diskonPersen.compareTo(new BigDecimal(AppConfig.MAX_DISCOUNT_PERCENTAGE)) > 0) {
            throw new IllegalArgumentException("Diskon maksimal " + AppConfig.MAX_DISCOUNT_PERCENTAGE + "%");
        }
        
        transaksiAktif.setDiskonPersen(diskonPersen != null ? diskonPersen : BigDecimal.ZERO);
        transaksiAktif.setDiskonNominal(diskonNominal != null ? diskonNominal : BigDecimal.ZERO);
        hitungUlangTransaksi();
    }
    
    /**
     * Set PPN untuk transaksi
     */
    public void setPPN(BigDecimal ppnPersen) {
        if (transaksiAktif == null) {
            throw new IllegalStateException("Belum ada transaksi aktif");
        }
        
        transaksiAktif.setPpnPersen(ppnPersen);
        hitungUlangTransaksi();
    }
    
    // ==================== PEMBAYARAN ====================
    
    /**
     * Proses pembayaran
     */
    public Transaksi prosesPembayaran(String metodePembayaran, BigDecimal jumlahBayar) {
        return prosesPembayaran(metodePembayaran, jumlahBayar, null);
    }
    
    /**
     * Proses pembayaran dengan referensi
     */
    public Transaksi prosesPembayaran(String metodePembayaran, BigDecimal jumlahBayar, String noReferensi) {
        if (transaksiAktif == null) {
            throw new IllegalStateException("Belum ada transaksi aktif");
        }
        
        if (keranjang.isEmpty()) {
            throw new IllegalStateException("Keranjang kosong");
        }
        
        // Validasi jumlah bayar
        if (jumlahBayar.compareTo(transaksiAktif.getGrandTotal()) < 0) {
            throw new IllegalArgumentException("Jumlah bayar kurang. Total: " + transaksiAktif.getGrandTotal());
        }
        
        // Buat pembayaran
        Pembayaran pembayaran = new Pembayaran(metodePembayaran, jumlahBayar);
        pembayaran.setNoReferensi(noReferensi);
        
        // Hitung kembalian
        BigDecimal kembalian = jumlahBayar.subtract(transaksiAktif.getGrandTotal());
        transaksiAktif.setTotalBayar(jumlahBayar);
        transaksiAktif.setKembalian(kembalian);
        
        // Tambahkan detail transaksi
        for (DetailTransaksi detail : keranjang) {
            transaksiAktif.addDetail(detail);
        }
        
        // Tambahkan pembayaran
        transaksiAktif.addPembayaran(pembayaran);
        transaksiAktif.setStatus(AppConfig.STATUS_COMPLETED);
        
        // Simpan transaksi
        Transaksi saved = transaksiRepository.save(transaksiAktif);
        
        // Update stok dan catat history
        for (DetailTransaksi detail : keranjang) {
            Produk produk = detail.getProduk();
            int stokSebelum = produk.getStok();
            int stokSesudah = stokSebelum - detail.getQty();
            
            produk.setStok(stokSesudah);
            produk.setTerjual(produk.getTerjual() + detail.getQty());
            produkRepository.update(produk);
            
            // Catat history stok
            StokHistory history = new StokHistory(produk, "KELUAR", detail.getQty(), stokSebelum, stokSesudah);
            history.setReferensiTipe("TRANSAKSI");
            history.setReferensiId(saved.getId());
            history.setReferensiNomor(saved.getNomorTransaksi());
            history.setKeterangan("Penjualan");
            history.setUser(transaksiAktif.getKasir());
            stokHistoryRepository.save(history);
        }
        
        // Update poin pelanggan jika member
        if (saved.getPelanggan() != null && saved.getPelanggan().isMember()) {
            int poin = saved.getGrandTotal().divide(new BigDecimal("10000"), 0, java.math.RoundingMode.DOWN).intValue();
            saved.setPoinDidapat(poin);
            saved.getPelanggan().tambahPoin(poin);
            saved.getPelanggan().tambahTransaksi(saved.getGrandTotal());
            pelangganRepository.update(saved.getPelanggan());
        }
        
        logger.info("Transaksi berhasil: {} - Total: {} - Bayar: {} - Kembalian: {}", 
                saved.getNomorTransaksi(), saved.getGrandTotal(), jumlahBayar, kembalian);
        
        // Reset transaksi aktif
        Transaksi completedTransaction = transaksiAktif;
        transaksiAktif = null;
        keranjang.clear();
        
        return completedTransaction;
    }
    
    /**
     * Batalkan transaksi aktif
     */
    public void batalkanTransaksiAktif() {
        if (transaksiAktif != null) {
            logger.info("Transaksi dibatalkan: {}", transaksiAktif.getNomorTransaksi());
        }
        transaksiAktif = null;
        keranjang.clear();
    }
    
    // ==================== QUERY TRANSAKSI ====================
    
    /**
     * Dapatkan transaksi aktif
     */
    public Transaksi getTransaksiAktif() {
        return transaksiAktif;
    }
    
    /**
     * Cari transaksi berdasarkan nomor
     */
    public Optional<Transaksi> getTransaksiByNomor(String nomorTransaksi) {
        return transaksiRepository.findByNomorTransaksi(nomorTransaksi);
    }
    
    /**
     * Dapatkan transaksi hari ini
     */
    public List<Transaksi> getTransaksiHariIni() {
        return transaksiRepository.findByTanggal(LocalDate.now());
    }
    
    /**
     * Dapatkan transaksi berdasarkan tanggal
     */
    public List<Transaksi> getTransaksiByTanggal(LocalDate tanggal) {
        return transaksiRepository.findByTanggal(tanggal);
    }
    
    /**
     * Dapatkan transaksi berdasarkan range tanggal
     */
    public List<Transaksi> getTransaksiByDateRange(LocalDate startDate, LocalDate endDate) {
        return transaksiRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Dapatkan transaksi berdasarkan kasir
     */
    public List<Transaksi> getTransaksiByKasir(Long kasirId) {
        return transaksiRepository.findByKasir(kasirId);
    }
    
    /**
     * Dapatkan transaksi terakhir
     */
    public List<Transaksi> getTransaksiTerakhir(int limit) {
        return transaksiRepository.findRecent(limit);
    }
    
    /**
     * Batalkan transaksi yang sudah selesai (void)
     */
    public void voidTransaksi(Long transaksiId, String alasan, User supervisor) {
        Optional<Transaksi> transaksiOpt = transaksiRepository.findById(transaksiId);
        if (transaksiOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaksi tidak ditemukan");
        }
        
        Transaksi transaksi = transaksiOpt.get();
        
        if (!AppConfig.STATUS_COMPLETED.equals(transaksi.getStatus())) {
            throw new IllegalStateException("Hanya transaksi yang sudah selesai yang dapat di-void");
        }
        
        // Kembalikan stok
        for (DetailTransaksi detail : transaksi.getDetailTransaksiList()) {
            Produk produk = detail.getProduk();
            int stokSebelum = produk.getStok();
            int stokSesudah = stokSebelum + detail.getQty();
            
            produk.setStok(stokSesudah);
            produk.setTerjual(produk.getTerjual() - detail.getQty());
            produkRepository.update(produk);
            
            // Catat history stok
            StokHistory history = new StokHistory(produk, "RETUR", detail.getQty(), stokSebelum, stokSesudah);
            history.setReferensiTipe("VOID_TRANSAKSI");
            history.setReferensiId(transaksi.getId());
            history.setReferensiNomor(transaksi.getNomorTransaksi());
            history.setKeterangan("Void transaksi: " + alasan);
            history.setUser(supervisor);
            stokHistoryRepository.save(history);
        }
        
        // Update status transaksi
        transaksiRepository.batalkanTransaksi(transaksiId, supervisor.getId(), alasan);
        
        // Kurangi poin pelanggan jika ada
        if (transaksi.getPelanggan() != null && transaksi.getPoinDidapat() > 0) {
            transaksi.getPelanggan().kurangiPoin(transaksi.getPoinDidapat());
            pelangganRepository.update(transaksi.getPelanggan());
        }
        
        logger.info("Transaksi di-void: {} - Alasan: {}", transaksi.getNomorTransaksi(), alasan);
    }
    
    // ==================== LAPORAN ====================
    
    /**
     * Dapatkan total penjualan hari ini
     */
    public BigDecimal getTotalPenjualanHariIni() {
        return transaksiRepository.getTotalPenjualanHariIni();
    }
    
    /**
     * Dapatkan jumlah transaksi hari ini
     */
    public long getJumlahTransaksiHariIni() {
        return transaksiRepository.countTransaksiHariIni();
    }
    
    /**
     * Dapatkan total penjualan per tanggal
     */
    public BigDecimal getTotalPenjualanByDate(LocalDate tanggal) {
        return transaksiRepository.getTotalPenjualanByDate(tanggal);
    }
}
