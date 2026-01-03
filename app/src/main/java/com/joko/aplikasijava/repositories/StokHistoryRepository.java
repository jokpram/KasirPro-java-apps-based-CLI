package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.models.StokHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository untuk entity StokHistory
 */
public class StokHistoryRepository extends GenericRepository<StokHistory, Long> {
    
    public StokHistoryRepository() {
        super(StokHistory.class);
    }
    
    /**
     * Cari history stok berdasarkan produk
     */
    public List<StokHistory> findByProduk(Long produkId) {
        String hql = "FROM StokHistory s WHERE s.produk.id = :produkId ORDER BY s.tanggal DESC";
        return executeQuery(hql, "produkId", produkId);
    }
    
    /**
     * Cari history stok berdasarkan tanggal
     */
    public List<StokHistory> findByTanggal(LocalDate tanggal) {
        LocalDateTime start = tanggal.atStartOfDay();
        LocalDateTime end = tanggal.plusDays(1).atStartOfDay();
        String hql = "FROM StokHistory s WHERE s.tanggal >= :start AND s.tanggal < :end ORDER BY s.tanggal DESC";
        return executeQuery(hql, "start", start, "end", end);
    }
    
    /**
     * Cari history stok berdasarkan tipe
     */
    public List<StokHistory> findByTipe(String tipe) {
        String hql = "FROM StokHistory s WHERE s.tipe = :tipe ORDER BY s.tanggal DESC";
        return executeQuery(hql, "tipe", tipe);
    }
    
    /**
     * Cari history stok berdasarkan produk dan range tanggal
     */
    public List<StokHistory> findByProdukAndDateRange(Long produkId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        String hql = "FROM StokHistory s WHERE s.produk.id = :produkId AND s.tanggal >= :start AND s.tanggal < :end ORDER BY s.tanggal DESC";
        return executeQuery(hql, "produkId", produkId, "start", start, "end", end);
    }
    
    /**
     * Cari history stok berdasarkan referensi
     */
    public List<StokHistory> findByReferensi(String referensiTipe, Long referensiId) {
        String hql = "FROM StokHistory s WHERE s.referensiTipe = :referensiTipe AND s.referensiId = :referensiId ORDER BY s.tanggal DESC";
        return executeQuery(hql, "referensiTipe", referensiTipe, "referensiId", referensiId);
    }
}
