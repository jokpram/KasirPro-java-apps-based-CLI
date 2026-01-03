package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.models.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity User
 */
public class UserRepository extends GenericRepository<User, Long> {
    
    public UserRepository() {
        super(User.class);
    }
    
    /**
     * Cari user berdasarkan username
     */
    public Optional<User> findByUsername(String username) {
        String hql = "FROM User u WHERE u.username = :username";
        return executeSingleQuery(hql, "username", username);
    }
    
    /**
     * Cari user berdasarkan email
     */
    public Optional<User> findByEmail(String email) {
        String hql = "FROM User u WHERE u.email = :email";
        return executeSingleQuery(hql, "email", email);
    }
    
    /**
     * Cari semua user aktif
     */
    public List<User> findAllActive() {
        String hql = "FROM User u WHERE u.aktif = true ORDER BY u.namaLengkap";
        return executeQuery(hql);
    }
    
    /**
     * Cari user berdasarkan role
     */
    public List<User> findByRole(String role) {
        String hql = "FROM User u WHERE u.role = :role AND u.aktif = true ORDER BY u.namaLengkap";
        return executeQuery(hql, "role", role);
    }
    
    /**
     * Cek apakah username sudah digunakan
     */
    public boolean isUsernameExists(String username) {
        return findByUsername(username).isPresent();
    }
    
    /**
     * Cek apakah email sudah digunakan
     */
    public boolean isEmailExists(String email) {
        if (email == null || email.isEmpty()) return false;
        return findByEmail(email).isPresent();
    }
    
    /**
     * Cari user yang terkunci
     */
    public List<User> findLockedUsers() {
        String hql = "FROM User u WHERE u.terkunci = true";
        return executeQuery(hql);
    }
    
    /**
     * Update status login terakhir
     */
    public void updateLastLogin(Long userId) {
        String hql = "UPDATE User u SET u.terakhirLogin = CURRENT_TIMESTAMP, u.jumlahLoginGagal = 0 WHERE u.id = :userId";
        executeUpdate(hql, "userId", userId);
    }
    
    /**
     * Increment login gagal
     */
    public void incrementLoginFailed(Long userId) {
        String hql = "UPDATE User u SET u.jumlahLoginGagal = u.jumlahLoginGagal + 1 WHERE u.id = :userId";
        executeUpdate(hql, "userId", userId);
    }
    
    /**
     * Lock user
     */
    public void lockUser(Long userId) {
        String hql = "UPDATE User u SET u.terkunci = true WHERE u.id = :userId";
        executeUpdate(hql, "userId", userId);
    }
    
    /**
     * Unlock user
     */
    public void unlockUser(Long userId) {
        String hql = "UPDATE User u SET u.terkunci = false, u.jumlahLoginGagal = 0 WHERE u.id = :userId";
        executeUpdate(hql, "userId", userId);
    }
    
    /**
     * Cari user berdasarkan nama (partial match)
     */
    public List<User> searchByName(String keyword) {
        String hql = "FROM User u WHERE LOWER(u.namaLengkap) LIKE :keyword AND u.aktif = true ORDER BY u.namaLengkap";
        return executeQuery(hql, "keyword", "%" + keyword.toLowerCase() + "%");
    }
    
    /**
     * Hitung user per role
     */
    public long countByRole(String role) {
        try (Session session = getSession()) {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.aktif = true";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("role", role);
            return query.getSingleResult();
        }
    }
}
