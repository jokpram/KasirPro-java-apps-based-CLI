package com.joko.aplikasijava.services;

import com.joko.aplikasijava.models.*;
import com.joko.aplikasijava.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk manajemen produk dan inventori
 */
public class ProdukService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProdukService.class);
    private final ProdukRepository produkRepository;
    private final KategoriRepository kategoriRepository;
    private final SupplierRepository supplierRepository;
    private final StokHistoryRepository stokHistoryRepository;
    
    public ProdukService() {
        this.produkRepository = new ProdukRepository();
        this.kategoriRepository = new KategoriRepository();
        this.supplierRepository = new SupplierRepository();
        this.stokHistoryRepository = new StokHistoryRepository();
    }
    
    // ==================== PRODUK ====================
    
    /**
     * Tambah produk baru
     */
    public Produk tambahProduk(Produk produk) {
        // Validasi kode unik
        if (produkRepository.isKodeExists(produk.getKode())) {
            throw new IllegalArgumentException("Kode produk sudah digunakan");
        }
        
        // Validasi barcode unik
        if (produk.getBarcode() != null && produkRepository.isBarcodeExists(produk.getBarcode())) {
            throw new IllegalArgumentException("Barcode sudah digunakan");
        }
        
        // Validasi harga
        if (produk.getHargaJual().compareTo(produk.getHargaBeli()) < 0) {
            logger.warn("Harga jual lebih kecil dari harga beli untuk produk: {}", produk.getKode());
        }
        
        Produk saved = produkRepository.save(produk);
        logger.info("Produk berhasil ditambahkan: {}", saved.getKode());
        
        // Catat stok awal jika ada
        if (saved.getStok() > 0) {
            catatStokHistory(saved, "MASUK", saved.getStok(), 0, saved.getStok(), "Stok awal");
        }
        
        return saved;
    }
    
    /**
     * Update produk
     */
    public Produk updateProduk(Produk produk) {
        Optional<Produk> existingOpt = produkRepository.findById(produk.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Produk tidak ditemukan");
        }
        
        Produk existing = existingOpt.get();
        
        // Cek perubahan kode
        if (!existing.getKode().equals(produk.getKode()) && produkRepository.isKodeExists(produk.getKode())) {
            throw new IllegalArgumentException("Kode produk sudah digunakan");
        }
        
        // Cek perubahan barcode
        if (produk.getBarcode() != null && !produk.getBarcode().equals(existing.getBarcode()) 
                && produkRepository.isBarcodeExists(produk.getBarcode())) {
            throw new IllegalArgumentException("Barcode sudah digunakan");
        }
        
        return produkRepository.update(produk);
    }
    
    /**
     * Hapus produk (soft delete)
     */
    public void hapusProduk(Long produkId) {
        Optional<Produk> produkOpt = produkRepository.findById(produkId);
        if (produkOpt.isPresent()) {
            Produk produk = produkOpt.get();
            produk.setAktif(false);
            produkRepository.update(produk);
            logger.info("Produk berhasil dinonaktifkan: {}", produk.getKode());
        }
    }
    
    /**
     * Cari produk berdasarkan ID
     */
    public Optional<Produk> getProdukById(Long id) {
        return produkRepository.findById(id);
    }
    
    /**
     * Cari produk berdasarkan kode atau barcode
     */
    public Optional<Produk> getProdukByKodeOrBarcode(String kodeOrBarcode) {
        Optional<Produk> produk = produkRepository.findByKode(kodeOrBarcode);
        if (produk.isEmpty()) {
            produk = produkRepository.findByBarcode(kodeOrBarcode);
        }
        return produk;
    }
    
    /**
     * Dapatkan semua produk aktif
     */
    public List<Produk> getAllProdukAktif() {
        return produkRepository.findAllActive();
    }
    
    /**
     * Cari produk
     */
    public List<Produk> cariProduk(String keyword) {
        return produkRepository.searchByKeyword(keyword);
    }
    
    /**
     * Dapatkan produk berdasarkan kategori
     */
    public List<Produk> getProdukByKategori(Long kategoriId) {
        return produkRepository.findByKategori(kategoriId);
    }
    
    /**
     * Dapatkan produk dengan stok rendah
     */
    public List<Produk> getProdukStokRendah() {
        return produkRepository.findLowStock();
    }
    
    /**
     * Dapatkan produk habis stok
     */
    public List<Produk> getProdukHabisStok() {
        return produkRepository.findOutOfStock();
    }
    
    /**
     * Dapatkan produk terlaris
     */
    public List<Produk> getProdukTerlaris(int limit) {
        return produkRepository.findBestSellers(limit);
    }
    
    /**
     * Dapatkan produk favorit
     */
    public List<Produk> getProdukFavorit() {
        return produkRepository.findFavorites();
    }
    
    /**
     * Toggle favorit produk
     */
    public void toggleFavorit(Long produkId) {
        Optional<Produk> produkOpt = produkRepository.findById(produkId);
        if (produkOpt.isPresent()) {
            Produk produk = produkOpt.get();
            produk.setFavorit(!produk.getFavorit());
            produkRepository.update(produk);
        }
    }
    
    // ==================== STOK ====================
    
    /**
     * Tambah stok produk
     */
    public void tambahStok(Long produkId, int qty, String keterangan, User user) {
        Optional<Produk> produkOpt = produkRepository.findById(produkId);
        if (produkOpt.isEmpty()) {
            throw new IllegalArgumentException("Produk tidak ditemukan");
        }
        
        Produk produk = produkOpt.get();
        int stokSebelum = produk.getStok();
        int stokSesudah = stokSebelum + qty;
        
        produk.setStok(stokSesudah);
        produkRepository.update(produk);
        
        catatStokHistory(produk, "MASUK", qty, stokSebelum, stokSesudah, keterangan, user);
        logger.info("Stok ditambahkan untuk produk {}: +{}", produk.getKode(), qty);
    }
    
    /**
     * Kurangi stok produk
     */
    public boolean kurangiStok(Long produkId, int qty, String keterangan, User user) {
        Optional<Produk> produkOpt = produkRepository.findById(produkId);
        if (produkOpt.isEmpty()) {
            throw new IllegalArgumentException("Produk tidak ditemukan");
        }
        
        Produk produk = produkOpt.get();
        if (produk.getStok() < qty) {
            logger.warn("Stok tidak mencukupi untuk produk {}: {}/{}", produk.getKode(), produk.getStok(), qty);
            return false;
        }
        
        int stokSebelum = produk.getStok();
        int stokSesudah = stokSebelum - qty;
        
        produk.setStok(stokSesudah);
        produkRepository.update(produk);
        
        catatStokHistory(produk, "KELUAR", qty, stokSebelum, stokSesudah, keterangan, user);
        logger.info("Stok dikurangi untuk produk {}: -{}", produk.getKode(), qty);
        return true;
    }
    
    /**
     * Adjustment stok (koreksi stok)
     */
    public void adjustmentStok(Long produkId, int stokBaru, String keterangan, User user) {
        Optional<Produk> produkOpt = produkRepository.findById(produkId);
        if (produkOpt.isEmpty()) {
            throw new IllegalArgumentException("Produk tidak ditemukan");
        }
        
        Produk produk = produkOpt.get();
        int stokSebelum = produk.getStok();
        int selisih = stokBaru - stokSebelum;
        
        produk.setStok(stokBaru);
        produkRepository.update(produk);
        
        catatStokHistory(produk, "ADJUSTMENT", Math.abs(selisih), stokSebelum, stokBaru, keterangan, user);
        logger.info("Adjustment stok untuk produk {}: {} -> {}", produk.getKode(), stokSebelum, stokBaru);
    }
    
    /**
     * Catat history stok
     */
    private void catatStokHistory(Produk produk, String tipe, int qty, int stokSebelum, int stokSesudah, String keterangan) {
        catatStokHistory(produk, tipe, qty, stokSebelum, stokSesudah, keterangan, null);
    }
    
    private void catatStokHistory(Produk produk, String tipe, int qty, int stokSebelum, int stokSesudah, String keterangan, User user) {
        StokHistory history = new StokHistory(produk, tipe, qty, stokSebelum, stokSesudah);
        history.setKeterangan(keterangan);
        history.setUser(user);
        stokHistoryRepository.save(history);
    }
    
    /**
     * Dapatkan history stok produk
     */
    public List<StokHistory> getStokHistory(Long produkId) {
        return stokHistoryRepository.findByProduk(produkId);
    }
    
    /**
     * Dapatkan total nilai stok
     */
    public BigDecimal getTotalNilaiStok() {
        return produkRepository.getTotalStockValue();
    }
    
    // ==================== KATEGORI ====================
    
    /**
     * Tambah kategori baru
     */
    public Kategori tambahKategori(Kategori kategori) {
        if (kategoriRepository.isKodeExists(kategori.getKode())) {
            throw new IllegalArgumentException("Kode kategori sudah digunakan");
        }
        return kategoriRepository.save(kategori);
    }
    
    /**
     * Update kategori
     */
    public Kategori updateKategori(Kategori kategori) {
        return kategoriRepository.update(kategori);
    }
    
    /**
     * Hapus kategori
     */
    public void hapusKategori(Long kategoriId) {
        // Cek apakah ada produk dalam kategori
        if (kategoriRepository.countProdukByKategori(kategoriId) > 0) {
            throw new IllegalStateException("Tidak dapat menghapus kategori yang masih memiliki produk");
        }
        
        Optional<Kategori> kategoriOpt = kategoriRepository.findById(kategoriId);
        if (kategoriOpt.isPresent()) {
            Kategori kategori = kategoriOpt.get();
            kategori.setAktif(false);
            kategoriRepository.update(kategori);
        }
    }
    
    /**
     * Dapatkan semua kategori aktif
     */
    public List<Kategori> getAllKategoriAktif() {
        return kategoriRepository.findAllActive();
    }
    
    /**
     * Dapatkan kategori parent
     */
    public List<Kategori> getKategoriParent() {
        return kategoriRepository.findParentCategories();
    }
    
    /**
     * Cari kategori berdasarkan kode
     */
    public Optional<Kategori> getKategoriByKode(String kode) {
        return kategoriRepository.findByKode(kode);
    }
    
    // ==================== SUPPLIER ====================
    
    /**
     * Tambah supplier baru
     */
    public Supplier tambahSupplier(Supplier supplier) {
        if (supplierRepository.isKodeExists(supplier.getKode())) {
            throw new IllegalArgumentException("Kode supplier sudah digunakan");
        }
        return supplierRepository.save(supplier);
    }
    
    /**
     * Update supplier
     */
    public Supplier updateSupplier(Supplier supplier) {
        return supplierRepository.update(supplier);
    }
    
    /**
     * Dapatkan semua supplier aktif
     */
    public List<Supplier> getAllSupplierAktif() {
        return supplierRepository.findAllActive();
    }
    
    /**
     * Cari supplier
     */
    public List<Supplier> cariSupplier(String keyword) {
        return supplierRepository.searchByKeyword(keyword);
    }
    
    /**
     * Generate kode supplier baru
     */
    public String generateKodeSupplier() {
        return supplierRepository.generateKode();
    }
    
    /**
     * Hitung total produk aktif
     */
    public long countProdukAktif() {
        return produkRepository.countActive();
    }
}
