package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.models.Produk;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Produk
 */
public class ProdukRepository extends GenericRepository<Produk, Long> {
    
    public ProdukRepository() {
        super(Produk.class);
    }
    
    /**
     * Cari produk berdasarkan kode
     */
    public Optional<Produk> findByKode(String kode) {
        String hql = "FROM Produk p WHERE p.kode = :kode";
        return executeSingleQuery(hql, "kode", kode);
    }
    
    /**
     * Cari produk berdasarkan barcode
     */
    public Optional<Produk> findByBarcode(String barcode) {
        String hql = "FROM Produk p WHERE p.barcode = :barcode";
        return executeSingleQuery(hql, "barcode", barcode);
    }
    
    /**
     * Cari semua produk aktif
     */
    public List<Produk> findAllActive() {
        String hql = "FROM Produk p WHERE p.aktif = true ORDER BY p.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari produk berdasarkan kategori
     */
    public List<Produk> findByKategori(Long kategoriId) {
        String hql = "FROM Produk p WHERE p.kategori.id = :kategoriId AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql, "kategoriId", kategoriId);
    }
    
    /**
     * Cari produk berdasarkan supplier
     */
    public List<Produk> findBySupplier(Long supplierId) {
        String hql = "FROM Produk p WHERE p.supplier.id = :supplierId AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql, "supplierId", supplierId);
    }
    
    /**
     * Cari produk dengan stok rendah
     */
    public List<Produk> findLowStock() {
        String hql = "FROM Produk p WHERE p.stok <= p.stokMinimum AND p.aktif = true ORDER BY p.stok";
        return executeQuery(hql);
    }
    
    /**
     * Cari produk habis stok
     */
    public List<Produk> findOutOfStock() {
        String hql = "FROM Produk p WHERE p.stok <= 0 AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari produk favorit
     */
    public List<Produk> findFavorites() {
        String hql = "FROM Produk p WHERE p.favorit = true AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari produk dengan keyword
     */
    public List<Produk> searchByKeyword(String keyword) {
        String hql = "FROM Produk p WHERE (LOWER(p.kode) LIKE :keyword OR LOWER(p.nama) LIKE :keyword OR LOWER(p.barcode) LIKE :keyword) AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql, "keyword", "%" + keyword.toLowerCase() + "%");
    }
    
    /**
     * Cari produk berdasarkan range harga
     */
    public List<Produk> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        String hql = "FROM Produk p WHERE p.hargaJual BETWEEN :minPrice AND :maxPrice AND p.aktif = true ORDER BY p.hargaJual";
        return executeQuery(hql, "minPrice", minPrice, "maxPrice", maxPrice);
    }
    
    /**
     * Cek apakah kode sudah digunakan
     */
    public boolean isKodeExists(String kode) {
        return findByKode(kode).isPresent();
    }
    
    /**
     * Cek apakah barcode sudah digunakan
     */
    public boolean isBarcodeExists(String barcode) {
        if (barcode == null || barcode.isEmpty()) return false;
        return findByBarcode(barcode).isPresent();
    }
    
    /**
     * Update stok produk
     */
    public void updateStok(Long produkId, int qty) {
        String hql = "UPDATE Produk p SET p.stok = p.stok + :qty WHERE p.id = :produkId";
        executeUpdate(hql, "qty", qty, "produkId", produkId);
    }
    
    /**
     * Kurangi stok produk
     */
    public boolean kurangiStok(Long produkId, int qty) {
        try (Session session = getSession()) {
            var transaction = session.beginTransaction();
            Produk produk = session.get(Produk.class, produkId);
            if (produk != null && produk.getStok() >= qty) {
                produk.setStok(produk.getStok() - qty);
                produk.setTerjual(produk.getTerjual() + qty);
                session.merge(produk);
                transaction.commit();
                return true;
            }
            transaction.rollback();
            return false;
        }
    }
    
    /**
     * Cari produk terlaris
     */
    public List<Produk> findBestSellers(int limit) {
        try (Session session = getSession()) {
            String hql = "FROM Produk p WHERE p.aktif = true ORDER BY p.terjual DESC";
            var query = session.createQuery(hql, Produk.class);
            query.setMaxResults(limit);
            return query.getResultList();
        }
    }
    
    /**
     * Hitung total nilai stok
     */
    public BigDecimal getTotalStockValue() {
        try (Session session = getSession()) {
            String hql = "SELECT SUM(p.hargaBeli * p.stok) FROM Produk p WHERE p.aktif = true";
            var query = session.createQuery(hql, BigDecimal.class);
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        }
    }
    
    /**
     * Hitung total produk aktif
     */
    public long countActive() {
        try (Session session = getSession()) {
            String hql = "SELECT COUNT(p) FROM Produk p WHERE p.aktif = true";
            var query = session.createQuery(hql, Long.class);
            return query.getSingleResult();
        }
    }
}
