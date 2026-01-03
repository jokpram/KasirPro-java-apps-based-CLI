package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.models.Supplier;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Supplier
 */
public class SupplierRepository extends GenericRepository<Supplier, Long> {
    
    public SupplierRepository() {
        super(Supplier.class);
    }
    
    /**
     * Cari supplier berdasarkan kode
     */
    public Optional<Supplier> findByKode(String kode) {
        String hql = "FROM Supplier s WHERE s.kode = :kode";
        return executeSingleQuery(hql, "kode", kode);
    }
    
    /**
     * Cari semua supplier aktif
     */
    public List<Supplier> findAllActive() {
        String hql = "FROM Supplier s WHERE s.aktif = true ORDER BY s.nama";
        return executeQuery(hql);
    }
    
    /**
     * Cari supplier dengan keyword
     */
    public List<Supplier> searchByKeyword(String keyword) {
        String hql = "FROM Supplier s WHERE (LOWER(s.kode) LIKE :keyword OR LOWER(s.nama) LIKE :keyword OR s.noTelepon LIKE :keyword) AND s.aktif = true ORDER BY s.nama";
        return executeQuery(hql, "keyword", "%" + keyword.toLowerCase() + "%");
    }
    
    /**
     * Cek apakah kode sudah digunakan
     */
    public boolean isKodeExists(String kode) {
        return findByKode(kode).isPresent();
    }
    
    /**
     * Generate kode supplier baru
     */
    public String generateKode() {
        try (var session = getSession()) {
            String hql = "SELECT MAX(s.kode) FROM Supplier s WHERE s.kode LIKE 'SUP%'";
            var query = session.createQuery(hql, String.class);
            String lastKode = query.getSingleResult();
            
            int nextNumber = 1;
            if (lastKode != null && lastKode.startsWith("SUP")) {
                try {
                    nextNumber = Integer.parseInt(lastKode.substring(3)) + 1;
                } catch (NumberFormatException e) {
                    // Use default
                }
            }
            return String.format("SUP%04d", nextNumber);
        }
    }
}
