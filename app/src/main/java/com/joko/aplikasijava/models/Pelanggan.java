package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity Pelanggan - Menyimpan data pelanggan/member
 * Termasuk informasi membership dan poin
 */
@Entity
@Table(name = "pelanggan", indexes = {
    @Index(name = "idx_pelanggan_kode", columnList = "kode_member"),
    @Index(name = "idx_pelanggan_nama", columnList = "nama"),
    @Index(name = "idx_pelanggan_telepon", columnList = "no_telepon")
})
public class Pelanggan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Size(max = 20, message = "Kode member maksimal 20 karakter")
    @Column(name = "kode_member", unique = true, length = 20)
    private String kodeMember;
    
    @NotBlank(message = "Nama pelanggan tidak boleh kosong")
    @Size(max = 100, message = "Nama pelanggan maksimal 100 karakter")
    @Column(name = "nama", nullable = false, length = 100)
    private String nama;
    
    @Column(name = "jenis_kelamin", length = 1)
    private String jenisKelamin; // L atau P
    
    @Column(name = "tanggal_lahir")
    private LocalDate tanggalLahir;
    
    @Column(name = "no_telepon", length = 20)
    private String noTelepon;
    
    @Email(message = "Format email tidak valid")
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "alamat", columnDefinition = "TEXT")
    private String alamat;
    
    @Column(name = "kota", length = 50)
    private String kota;
    
    @Column(name = "provinsi", length = 50)
    private String provinsi;
    
    @Column(name = "kode_pos", length = 10)
    private String kodePos;
    
    @Column(name = "no_ktp", length = 20)
    private String noKtp;
    
    @Column(name = "poin", nullable = false)
    private Integer poin = 0;
    
    @Column(name = "total_belanja", precision = 15, scale = 2)
    private BigDecimal totalBelanja = BigDecimal.ZERO;
    
    @Column(name = "jumlah_transaksi")
    private Integer jumlahTransaksi = 0;
    
    @Column(name = "tipe_member", length = 20)
    private String tipeMember = "REGULAR"; // REGULAR, SILVER, GOLD, PLATINUM
    
    @Column(name = "diskon_member", precision = 5, scale = 2)
    private BigDecimal diskonMember = BigDecimal.ZERO;
    
    @Column(name = "tanggal_bergabung")
    private LocalDate tanggalBergabung;
    
    @Column(name = "tanggal_kadaluarsa_member")
    private LocalDate tanggalKadaluarsaMember;
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "aktif", nullable = false)
    private Boolean aktif = true;
    
    @OneToMany(mappedBy = "pelanggan", cascade = CascadeType.ALL)
    private List<Transaksi> transaksiList = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Pelanggan() {}
    
    public Pelanggan(String nama) {
        this.nama = nama;
    }
    
    public Pelanggan(String kodeMember, String nama, String noTelepon) {
        this.kodeMember = kodeMember;
        this.nama = nama;
        this.noTelepon = noTelepon;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (tanggalBergabung == null) {
            tanggalBergabung = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public void tambahPoin(int poin) {
        this.poin += poin;
        updateTipeMember();
    }
    
    public boolean kurangiPoin(int poin) {
        if (this.poin >= poin) {
            this.poin -= poin;
            return true;
        }
        return false;
    }
    
    public void tambahTransaksi(BigDecimal nominal) {
        this.totalBelanja = this.totalBelanja.add(nominal);
        this.jumlahTransaksi++;
        updateTipeMember();
    }
    
    private void updateTipeMember() {
        if (totalBelanja.compareTo(new BigDecimal("50000000")) >= 0) {
            tipeMember = "PLATINUM";
            diskonMember = new BigDecimal("15");
        } else if (totalBelanja.compareTo(new BigDecimal("20000000")) >= 0) {
            tipeMember = "GOLD";
            diskonMember = new BigDecimal("10");
        } else if (totalBelanja.compareTo(new BigDecimal("5000000")) >= 0) {
            tipeMember = "SILVER";
            diskonMember = new BigDecimal("5");
        } else {
            tipeMember = "REGULAR";
            diskonMember = BigDecimal.ZERO;
        }
    }
    
    public boolean isMember() {
        return kodeMember != null && !kodeMember.isEmpty();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getKodeMember() { return kodeMember; }
    public void setKodeMember(String kodeMember) { this.kodeMember = kodeMember; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getJenisKelamin() { return jenisKelamin; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }
    
    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(LocalDate tanggalLahir) { this.tanggalLahir = tanggalLahir; }
    
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    
    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }
    
    public String getProvinsi() { return provinsi; }
    public void setProvinsi(String provinsi) { this.provinsi = provinsi; }
    
    public String getKodePos() { return kodePos; }
    public void setKodePos(String kodePos) { this.kodePos = kodePos; }
    
    public String getNoKtp() { return noKtp; }
    public void setNoKtp(String noKtp) { this.noKtp = noKtp; }
    
    public Integer getPoin() { return poin; }
    public void setPoin(Integer poin) { this.poin = poin; }
    
    public BigDecimal getTotalBelanja() { return totalBelanja; }
    public void setTotalBelanja(BigDecimal totalBelanja) { this.totalBelanja = totalBelanja; }
    
    public Integer getJumlahTransaksi() { return jumlahTransaksi; }
    public void setJumlahTransaksi(Integer jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }
    
    public String getTipeMember() { return tipeMember; }
    public void setTipeMember(String tipeMember) { this.tipeMember = tipeMember; }
    
    public BigDecimal getDiskonMember() { return diskonMember; }
    public void setDiskonMember(BigDecimal diskonMember) { this.diskonMember = diskonMember; }
    
    public LocalDate getTanggalBergabung() { return tanggalBergabung; }
    public void setTanggalBergabung(LocalDate tanggalBergabung) { this.tanggalBergabung = tanggalBergabung; }
    
    public LocalDate getTanggalKadaluarsaMember() { return tanggalKadaluarsaMember; }
    public void setTanggalKadaluarsaMember(LocalDate tanggalKadaluarsaMember) { this.tanggalKadaluarsaMember = tanggalKadaluarsaMember; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
    
    public List<Transaksi> getTransaksiList() { return transaksiList; }
    public void setTransaksiList(List<Transaksi> transaksiList) { this.transaksiList = transaksiList; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pelanggan pelanggan = (Pelanggan) o;
        return Objects.equals(id, pelanggan.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Pelanggan{" +
                "id=" + id +
                ", kodeMember='" + kodeMember + '\'' +
                ", nama='" + nama + '\'' +
                ", tipeMember='" + tipeMember + '\'' +
                ", poin=" + poin +
                '}';
    }
}
