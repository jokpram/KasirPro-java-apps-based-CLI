package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.models.Pelanggan;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Pelanggan
 */
public class PelangganRepository extends GenericRepository<Pelanggan, Long> {
    
    public PelangganRepository() {
        super(Pelanggan.class);
    }
    
    /**
     * Cari pelanggan berdasarkan kode member
     */
    public Optional<Pelanggan> findByKodeMember(String kodeMember) {
        String hql = "FROM Pelanggan p WHERE p.kodeMember = :kodeMember";
        return executeSingleQuery(hql, "kodeMember", kodeMember);
    }
    
    /**
     * Cari pelanggan berdasarkan no telepon
     */
    public Optional<Pelanggan> findByNoTelepon(String noTelepon) {
        String hql = "FROM Pelanggan p WHERE p.noTelepon = :noTelepon";
        return executeSingleQuery(hql, "noTelepon", noTelepon);
    }
    
    /**
     * Cari pelanggan berdasarkan email
     */
    public Optional<Pelanggan> findByEmail(String email) {
        String hql = "FROM Pelanggan p WHERE p.email = :email";
        return executeSingleQuery(hql, "email", email);
    }
    
    /**
     * Cari semua pelanggan aktif
     */
    public List<Pelanggan> findAllActive() {
        String hql = "FROM Pelanggan p WHERE p.aktif = true ORDER BY p.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari pelanggan member (yang punya kode member)
     */
    public List<Pelanggan> findMembers() {
        String hql = "FROM Pelanggan p WHERE p.kodeMember IS NOT NULL AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari pelanggan berdasarkan tipe member
     */
    public List<Pelanggan> findByTipeMember(String tipeMember) {
        String hql = "FROM Pelanggan p WHERE p.tipeMember = :tipeMember AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql, "tipeMember", tipeMember);
    }
    
    /**
     * Cari pelanggan dengan keyword
     */
    public List<Pelanggan> searchByKeyword(String keyword) {
        String hql = "FROM Pelanggan p WHERE (LOWER(p.nama) LIKE :keyword OR LOWER(p.kodeMember) LIKE :keyword OR p.noTelepon LIKE :keyword) AND p.aktif = true ORDER BY p.nama";
        return executeQuery(hql, "keyword", "%" + keyword.toLowerCase() + "%");
    }
    
    /**
     * Cari pelanggan dengan poin tertinggi
     */
    public List<Pelanggan> findTopByPoin(int limit) {
        try (var session = getSession()) {
            String hql = "FROM Pelanggan p WHERE p.aktif = true ORDER BY p.poin DESC";
            var query = session.createQuery(hql, Pelanggan.class);
            query.setMaxResults(limit);
            return query.getResultList();
        }
    }
    
    /**
     * Cari pelanggan dengan total belanja tertinggi
     */
    public List<Pelanggan> findTopByTotalBelanja(int limit) {
        try (var session = getSession()) {
            String hql = "FROM Pelanggan p WHERE p.aktif = true ORDER BY p.totalBelanja DESC";
            var query = session.createQuery(hql, Pelanggan.class);
            query.setMaxResults(limit);
            return query.getResultList();
        }
    }
    
    /**
     * Update poin pelanggan
     */
    public void updatePoin(Long pelangganId, int poin) {
        String hql = "UPDATE Pelanggan p SET p.poin = p.poin + :poin WHERE p.id = :pelangganId";
        executeUpdate(hql, "poin", poin, "pelangganId", pelangganId);
    }
    
    /**
     * Update total belanja pelanggan
     */
    public void updateTotalBelanja(Long pelangganId, BigDecimal nominal) {
        try (var session = getSession()) {
            var transaction = session.beginTransaction();
            Pelanggan pelanggan = session.get(Pelanggan.class, pelangganId);
            if (pelanggan != null) {
                pelanggan.tambahTransaksi(nominal);
                session.merge(pelanggan);
            }
            transaction.commit();
        }
    }
    
    /**
     * Generate kode member baru
     */
    public String generateKodeMember() {
        try (var session = getSession()) {
            String hql = "SELECT MAX(p.kodeMember) FROM Pelanggan p WHERE p.kodeMember LIKE 'MBR%'";
            var query = session.createQuery(hql, String.class);
            String lastKode = query.getSingleResult();
            
            int nextNumber = 1;
            if (lastKode != null && lastKode.startsWith("MBR")) {
                try {
                    nextNumber = Integer.parseInt(lastKode.substring(3)) + 1;
                } catch (NumberFormatException e) {
                    // Use default
                }
            }
            return String.format("MBR%06d", nextNumber);
        }
    }
    
    /**
     * Hitung total member
     */
    public long countMembers() {
        try (var session = getSession()) {
            String hql = "SELECT COUNT(p) FROM Pelanggan p WHERE p.kodeMember IS NOT NULL AND p.aktif = true";
            var query = session.createQuery(hql, Long.class);
            return query.getSingleResult();
        }
    }
}
