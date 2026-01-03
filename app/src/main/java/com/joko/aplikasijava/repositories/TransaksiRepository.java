package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.models.Transaksi;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Transaksi
 */
public class TransaksiRepository extends GenericRepository<Transaksi, Long> {
    
    public TransaksiRepository() {
        super(Transaksi.class);
    }
    
    /**
     * Cari transaksi berdasarkan nomor
     */
    public Optional<Transaksi> findByNomorTransaksi(String nomorTransaksi) {
        String hql = "FROM Transaksi t WHERE t.nomorTransaksi = :nomorTransaksi";
        return executeSingleQuery(hql, "nomorTransaksi", nomorTransaksi);
    }
    
    /**
     * Cari transaksi berdasarkan nomor invoice
     */
    public Optional<Transaksi> findByNomorInvoice(String nomorInvoice) {
        String hql = "FROM Transaksi t WHERE t.nomorInvoice = :nomorInvoice";
        return executeSingleQuery(hql, "nomorInvoice", nomorInvoice);
    }
    
    /**
     * Cari transaksi berdasarkan tanggal
     */
    public List<Transaksi> findByTanggal(LocalDate tanggal) {
        LocalDateTime start = tanggal.atStartOfDay();
        LocalDateTime end = tanggal.plusDays(1).atStartOfDay();
        String hql = "FROM Transaksi t WHERE t.tanggalTransaksi >= :start AND t.tanggalTransaksi < :end ORDER BY t.tanggalTransaksi DESC";
        return executeQuery(hql, "start", start, "end", end);
    }
    
    /**
     * Cari transaksi berdasarkan range tanggal
     */
    public List<Transaksi> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        String hql = "FROM Transaksi t WHERE t.tanggalTransaksi >= :start AND t.tanggalTransaksi < :end ORDER BY t.tanggalTransaksi DESC";
        return executeQuery(hql, "start", start, "end", end);
    }
    
    /**
     * Cari transaksi berdasarkan kasir
     */
    public List<Transaksi> findByKasir(Long kasirId) {
        String hql = "FROM Transaksi t WHERE t.kasir.id = :kasirId ORDER BY t.tanggalTransaksi DESC";
        return executeQuery(hql, "kasirId", kasirId);
    }
    
    /**
     * Cari transaksi berdasarkan kasir dan tanggal
     */
    public List<Transaksi> findByKasirAndTanggal(Long kasirId, LocalDate tanggal) {
        LocalDateTime start = tanggal.atStartOfDay();
        LocalDateTime end = tanggal.plusDays(1).atStartOfDay();
        String hql = "FROM Transaksi t WHERE t.kasir.id = :kasirId AND t.tanggalTransaksi >= :start AND t.tanggalTransaksi < :end ORDER BY t.tanggalTransaksi DESC";
        return executeQuery(hql, "kasirId", kasirId, "start", start, "end", end);
    }
    
    /**
     * Cari transaksi berdasarkan pelanggan
     */
    public List<Transaksi> findByPelanggan(Long pelangganId) {
        String hql = "FROM Transaksi t WHERE t.pelanggan.id = :pelangganId ORDER BY t.tanggalTransaksi DESC";
        return executeQuery(hql, "pelangganId", pelangganId);
    }
    
    /**
     * Cari transaksi berdasarkan status
     */
    public List<Transaksi> findByStatus(String status) {
        String hql = "FROM Transaksi t WHERE t.status = :status ORDER BY t.tanggalTransaksi DESC";
        return executeQuery(hql, "status", status);
    }
    
    /**
     * Cari transaksi pending
     */
    public List<Transaksi> findPending() {
        return findByStatus("PENDING");
    }
    
    /**
     * Cari transaksi selesai
     */
    public List<Transaksi> findCompleted() {
        return findByStatus("SELESAI");
    }
    
    /**
     * Generate nomor transaksi baru
     */
    public String generateNomorTransaksi() {
        try (Session session = getSession()) {
            LocalDate today = LocalDate.now();
            String prefix = String.format("TRX%s", today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            String hql = "SELECT MAX(t.nomorTransaksi) FROM Transaksi t WHERE t.nomorTransaksi LIKE :prefix";
            var query = session.createQuery(hql, String.class);
            query.setParameter("prefix", prefix + "%");
            String lastNo = query.getSingleResult();
            
            int nextNumber = 1;
            if (lastNo != null && lastNo.startsWith(prefix)) {
                try {
                    nextNumber = Integer.parseInt(lastNo.substring(prefix.length())) + 1;
                } catch (NumberFormatException e) {
                    // Use default
                }
            }
            return String.format("%s%04d", prefix, nextNumber);
        }
    }
    
    /**
     * Hitung total penjualan hari ini
     */
    public BigDecimal getTotalPenjualanHariIni() {
        try (Session session = getSession()) {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.plusDays(1).atStartOfDay();
            
            String hql = "SELECT COALESCE(SUM(t.grandTotal), 0) FROM Transaksi t WHERE t.tanggalTransaksi >= :start AND t.tanggalTransaksi < :end AND t.status = 'SELESAI'";
            var query = session.createQuery(hql, BigDecimal.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        }
    }
    
    /**
     * Hitung jumlah transaksi hari ini
     */
    public long countTransaksiHariIni() {
        try (Session session = getSession()) {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.plusDays(1).atStartOfDay();
            
            String hql = "SELECT COUNT(t) FROM Transaksi t WHERE t.tanggalTransaksi >= :start AND t.tanggalTransaksi < :end AND t.status = 'SELESAI'";
            var query = session.createQuery(hql, Long.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        }
    }
    
    /**
     * Hitung total penjualan per tanggal
     */
    public BigDecimal getTotalPenjualanByDate(LocalDate tanggal) {
        try (Session session = getSession()) {
            LocalDateTime start = tanggal.atStartOfDay();
            LocalDateTime end = tanggal.plusDays(1).atStartOfDay();
            
            String hql = "SELECT COALESCE(SUM(t.grandTotal), 0) FROM Transaksi t WHERE t.tanggalTransaksi >= :start AND t.tanggalTransaksi < :end AND t.status = 'SELESAI'";
            var query = session.createQuery(hql, BigDecimal.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        }
    }
    
    /**
     * Batalkan transaksi
     */
    public void batalkanTransaksi(Long transaksiId, Long userId, String alasan) {
        try (Session session = getSession()) {
            var transaction = session.beginTransaction();
            Transaksi transaksi = session.get(Transaksi.class, transaksiId);
            if (transaksi != null) {
                transaksi.setStatus("DIBATALKAN");
                transaksi.setAlasanPembatalan(alasan);
                transaksi.setDibatalkanOleh(userId);
                transaksi.setTanggalPembatalan(LocalDateTime.now());
                session.merge(transaksi);
            }
            transaction.commit();
        }
    }
    
    /**
     * Cari transaksi terakhir
     */
    public List<Transaksi> findRecent(int limit) {
        try (Session session = getSession()) {
            String hql = "FROM Transaksi t ORDER BY t.tanggalTransaksi DESC";
            var query = session.createQuery(hql, Transaksi.class);
            query.setMaxResults(limit);
            return query.getResultList();
        }
    }
}
