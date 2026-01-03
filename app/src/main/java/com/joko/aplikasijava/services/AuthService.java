package com.joko.aplikasijava.services;

import com.joko.aplikasijava.config.AppConfig;
import com.joko.aplikasijava.models.User;
import com.joko.aplikasijava.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk autentikasi dan manajemen user
 */
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private User currentUser;
    
    public AuthService() {
        this.userRepository = new UserRepository();
    }
    
    /**
     * Login user dengan username dan password
     * @return User jika berhasil, null jika gagal
     */
    public User login(String username, String password) {
        logger.info("Mencoba login untuk user: {}", username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            logger.warn("User tidak ditemukan: {}", username);
            return null;
        }
        
        User user = userOpt.get();
        
        // Cek apakah user aktif
        if (!user.getAktif()) {
            logger.warn("User tidak aktif: {}", username);
            return null;
        }
        
        // Cek apakah user terkunci
        if (user.getTerkunci()) {
            logger.warn("User terkunci: {}", username);
            return null;
        }
        
        // Verifikasi password
        if (!BCrypt.checkpw(password, user.getPassword())) {
            logger.warn("Password salah untuk user: {}", username);
            
            // Increment login gagal
            userRepository.incrementLoginFailed(user.getId());
            user.setJumlahLoginGagal(user.getJumlahLoginGagal() + 1);
            
            // Lock user jika sudah melebihi batas
            if (user.getJumlahLoginGagal() >= AppConfig.MAX_LOGIN_ATTEMPTS) {
                userRepository.lockUser(user.getId());
                logger.warn("User terkunci karena terlalu banyak login gagal: {}", username);
            }
            
            return null;
        }
        
        // Login berhasil
        user.setTerakhirLogin(LocalDateTime.now());
        user.setJumlahLoginGagal(0);
        userRepository.updateLastLogin(user.getId());
        
        currentUser = user;
        logger.info("Login berhasil untuk user: {}", username);
        
        return user;
    }
    
    /**
     * Logout user
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("Logout user: {}", currentUser.getUsername());
            currentUser = null;
        }
    }
    
    /**
     * Dapatkan user yang sedang login
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Cek apakah ada user yang login
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Cek apakah user saat ini adalah admin
     */
    public boolean isAdmin() {
        return currentUser != null && AppConfig.ROLE_ADMIN.equals(currentUser.getRole());
    }
    
    /**
     * Cek apakah user saat ini adalah supervisor
     */
    public boolean isSupervisor() {
        return currentUser != null && (AppConfig.ROLE_SUPERVISOR.equals(currentUser.getRole()) || isAdmin());
    }
    
    /**
     * Cek apakah user saat ini adalah kasir
     */
    public boolean isKasir() {
        return currentUser != null;
    }
    
    /**
     * Register user baru
     */
    public User register(String username, String password, String namaLengkap, String role) {
        // Validasi username
        if (userRepository.isUsernameExists(username)) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }
        
        // Validasi password
        if (password.length() < AppConfig.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password minimal " + AppConfig.MIN_PASSWORD_LENGTH + " karakter");
        }
        
        // Hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Create user
        User user = new User(username, hashedPassword, namaLengkap, role);
        if (currentUser != null) {
            user.setCreatedBy(currentUser.getId());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Ganti password user
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Verifikasi password lama
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            return false;
        }
        
        // Validasi password baru
        if (newPassword.length() < AppConfig.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password minimal " + AppConfig.MIN_PASSWORD_LENGTH + " karakter");
        }
        
        // Update password
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.update(user);
        
        logger.info("Password berhasil diubah untuk user: {}", user.getUsername());
        return true;
    }
    
    /**
     * Reset password user (untuk admin)
     */
    public boolean resetPassword(Long userId, String newPassword) {
        if (!isAdmin()) {
            throw new SecurityException("Hanya admin yang dapat reset password");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        user.setTerkunci(false);
        user.setJumlahLoginGagal(0);
        userRepository.update(user);
        
        logger.info("Password berhasil direset untuk user: {}", user.getUsername());
        return true;
    }
    
    /**
     * Unlock user (untuk admin)
     */
    public boolean unlockUser(Long userId) {
        if (!isAdmin()) {
            throw new SecurityException("Hanya admin yang dapat unlock user");
        }
        
        userRepository.unlockUser(userId);
        logger.info("User berhasil di-unlock: {}", userId);
        return true;
    }
    
    /**
     * Dapatkan semua user
     */
    public List<User> getAllUsers() {
        return userRepository.findAllActive();
    }
    
    /**
     * Dapatkan user berdasarkan role
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Update profil user
     */
    public User updateProfile(Long userId, String namaLengkap, String email, String noTelepon, String alamat) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User tidak ditemukan");
        }
        
        User user = userOpt.get();
        user.setNamaLengkap(namaLengkap);
        user.setEmail(email);
        user.setNoTelepon(noTelepon);
        user.setAlamat(alamat);
        
        return userRepository.update(user);
    }
    
    /**
     * Inisialisasi admin default jika belum ada
     */
    public void initDefaultAdmin() {
        if (userRepository.countByRole(AppConfig.ROLE_ADMIN) == 0) {
            logger.info("Membuat admin default...");
            String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
            User admin = new User("admin", hashedPassword, "Administrator", AppConfig.ROLE_ADMIN);
            admin.setEmail("admin@kasirpro.com");
            userRepository.save(admin);
            logger.info("Admin default berhasil dibuat. Username: admin, Password: admin123");
        }
    }
}
