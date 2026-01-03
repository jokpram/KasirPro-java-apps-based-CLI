package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.models.Kategori;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Kategori
 */
public class KategoriRepository extends GenericRepository<Kategori, Long> {
    
    public KategoriRepository() {
        super(Kategori.class);
    }
    
    /**
     * Cari kategori berdasarkan kode
     */
    public Optional<Kategori> findByKode(String kode) {
        String hql = "FROM Kategori k WHERE k.kode = :kode";
        return executeSingleQuery(hql, "kode", kode);
    }
    
    /**
     * Cari kategori berdasarkan nama
     */
    public Optional<Kategori> findByNama(String nama) {
        String hql = "FROM Kategori k WHERE k.nama = :nama";
        return executeSingleQuery(hql, "nama", nama);
    }
    
    /**
     * Cari semua kategori aktif
     */
    public List<Kategori> findAllActive() {
        String hql = "FROM Kategori k WHERE k.aktif = true ORDER BY k.urutan, k.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari kategori parent (yang tidak punya parent)
     */
    public List<Kategori> findParentCategories() {
        String hql = "FROM Kategori k WHERE k.parent IS NULL AND k.aktif = true ORDER BY k.urutan, k.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari kategori child berdasarkan parent id
     */
    public List<Kategori> findByParentId(Long parentId) {
        String hql = "FROM Kategori k WHERE k.parent.id = :parentId AND k.aktif = true ORDER BY k.urutan, k.nama";
        return executeQuery(hql, "parentId", parentId);
    }
    
    /**
     * Cek apakah kode sudah digunakan
     */
    public boolean isKodeExists(String kode) {
        return findByKode(kode).isPresent();
    }
    
    /**
     * Cari kategori berdasarkan keyword
     */
    public List<Kategori> searchByKeyword(String keyword) {
        String hql = "FROM Kategori k WHERE (LOWER(k.kode) LIKE :keyword OR LOWER(k.nama) LIKE :keyword) AND k.aktif = true ORDER BY k.nama";
        return executeQuery(hql, "keyword", "%" + keyword.toLowerCase() + "%");
    }
    
    /**
     * Hitung jumlah produk dalam kategori
     */
    public long countProdukByKategori(Long kategoriId) {
        try (var session = getSession()) {
            String hql = "SELECT COUNT(p) FROM Produk p WHERE p.kategori.id = :kategoriId AND p.aktif = true";
            var query = session.createQuery(hql, Long.class);
            query.setParameter("kategoriId", kategoriId);
            return query.getSingleResult();
        }
    }
}
