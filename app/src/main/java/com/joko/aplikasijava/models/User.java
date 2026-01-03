package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity User - Menyimpan data pengguna sistem kasir
 * Mendukung role: ADMIN, KASIR, SUPERVISOR
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email")
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus antara 3-50 karakter")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "Password tidak boleh kosong")
    @Column(name = "password", nullable = false)
    private String password;
    
    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
    @Column(name = "nama_lengkap", nullable = false, length = 100)
    private String namaLengkap;
    
    @Email(message = "Format email tidak valid")
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    @Column(name = "no_telepon", length = 20)
    private String noTelepon;
    
    @Column(name = "alamat", columnDefinition = "TEXT")
    private String alamat;
    
    @NotBlank(message = "Role tidak boleh kosong")
    @Column(name = "role", nullable = false, length = 20)
    private String role; // ADMIN, KASIR, SUPERVISOR
    
    @Column(name = "aktif", nullable = false)
    private Boolean aktif = true;
    
    @Column(name = "foto_profil")
    private String fotoProfil;
    
    @Column(name = "terakhir_login")
    private LocalDateTime terakhirLogin;
    
    @Column(name = "jumlah_login_gagal")
    private Integer jumlahLoginGagal = 0;
    
    @Column(name = "terkunci")
    private Boolean terkunci = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    // Constructors
    public User() {}
    
    public User(String username, String password, String namaLengkap, String role) {
        this.username = username;
        this.password = password;
        this.namaLengkap = namaLengkap;
        this.role = role;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
    
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
    
    public String getFotoProfil() { return fotoProfil; }
    public void setFotoProfil(String fotoProfil) { this.fotoProfil = fotoProfil; }
    
    public LocalDateTime getTerakhirLogin() { return terakhirLogin; }
    public void setTerakhirLogin(LocalDateTime terakhirLogin) { this.terakhirLogin = terakhirLogin; }
    
    public Integer getJumlahLoginGagal() { return jumlahLoginGagal; }
    public void setJumlahLoginGagal(Integer jumlahLoginGagal) { this.jumlahLoginGagal = jumlahLoginGagal; }
    
    public Boolean getTerkunci() { return terkunci; }
    public void setTerkunci(Boolean terkunci) { this.terkunci = terkunci; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", namaLengkap='" + namaLengkap + '\'' +
                ", role='" + role + '\'' +
                ", aktif=" + aktif +
                '}';
    }
}
