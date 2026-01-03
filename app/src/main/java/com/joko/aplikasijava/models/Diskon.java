package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity Diskon - Menyimpan data diskon/promosi
 */
@Entity
@Table(name = "diskon", indexes = {
    @Index(name = "idx_diskon_kode", columnList = "kode"),
    @Index(name = "idx_diskon_tanggal", columnList = "tanggal_mulai, tanggal_selesai")
})
public class Diskon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Kode diskon tidak boleh kosong")
    @Size(max = 30, message = "Kode diskon maksimal 30 karakter")
    @Column(name = "kode", unique = true, nullable = false, length = 30)
    private String kode;
    
    @NotBlank(message = "Nama diskon tidak boleh kosong")
    @Size(max = 100, message = "Nama diskon maksimal 100 karakter")
    @Column(name = "nama", nullable = false, length = 100)
    private String nama;
    
    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;
    
    @Column(name = "tipe", nullable = false, length = 20)
    private String tipe = "PERSEN"; // PERSEN, NOMINAL, BELI_X_GRATIS_Y
    
    @Column(name = "nilai", precision = 15, scale = 2)
    private BigDecimal nilai; // Persen atau nominal
    
    @Column(name = "min_pembelian", precision = 15, scale = 2)
    private BigDecimal minPembelian = BigDecimal.ZERO;
    
    @Column(name = "max_diskon", precision = 15, scale = 2)
    private BigDecimal maxDiskon; // Maksimal diskon untuk tipe persen
    
    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;
    
    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDate tanggalSelesai;
    
    @Column(name = "berlaku_untuk", length = 20)
    private String berlakuUntuk = "SEMUA"; // SEMUA, KATEGORI, PRODUK, MEMBER
    
    @Column(name = "kategori_id")
    private Long kategoriId;
    
    @Column(name = "produk_id")
    private Long produkId;
    
    @Column(name = "tipe_member", length = 20)
    private String tipeMember;
    
    @Column(name = "kuota")
    private Integer kuota; // Jumlah maksimal penggunaan
    
    @Column(name = "terpakai")
    private Integer terpakai = 0;
    
    @Column(name = "aktif", nullable = false)
    private Boolean aktif = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Diskon() {}
    
    public Diskon(String kode, String nama, String tipe, BigDecimal nilai, LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        this.kode = kode;
        this.nama = nama;
        this.tipe = tipe;
        this.nilai = nilai;
        this.tanggalMulai = tanggalMulai;
        this.tanggalSelesai = tanggalSelesai;
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
    
    // Business methods
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return aktif && 
               today.compareTo(tanggalMulai) >= 0 && 
               today.compareTo(tanggalSelesai) <= 0 &&
               (kuota == null || terpakai < kuota);
    }
    
    public BigDecimal hitungDiskon(BigDecimal totalBelanja) {
        if (!isValid() || totalBelanja.compareTo(minPembelian) < 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal diskon;
        if ("PERSEN".equals(tipe)) {
            diskon = totalBelanja.multiply(nilai).divide(new BigDecimal("100"));
            if (maxDiskon != null && diskon.compareTo(maxDiskon) > 0) {
                diskon = maxDiskon;
            }
        } else {
            diskon = nilai;
        }
        
        return diskon;
    }
    
    public void incrementTerpakai() {
        this.terpakai++;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }
    
    public BigDecimal getNilai() { return nilai; }
    public void setNilai(BigDecimal nilai) { this.nilai = nilai; }
    
    public BigDecimal getMinPembelian() { return minPembelian; }
    public void setMinPembelian(BigDecimal minPembelian) { this.minPembelian = minPembelian; }
    
    public BigDecimal getMaxDiskon() { return maxDiskon; }
    public void setMaxDiskon(BigDecimal maxDiskon) { this.maxDiskon = maxDiskon; }
    
    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }
    
    public LocalDate getTanggalSelesai() { return tanggalSelesai; }
    public void setTanggalSelesai(LocalDate tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }
    
    public String getBerlakuUntuk() { return berlakuUntuk; }
    public void setBerlakuUntuk(String berlakuUntuk) { this.berlakuUntuk = berlakuUntuk; }
    
    public Long getKategoriId() { return kategoriId; }
    public void setKategoriId(Long kategoriId) { this.kategoriId = kategoriId; }
    
    public Long getProdukId() { return produkId; }
    public void setProdukId(Long produkId) { this.produkId = produkId; }
    
    public String getTipeMember() { return tipeMember; }
    public void setTipeMember(String tipeMember) { this.tipeMember = tipeMember; }
    
    public Integer getKuota() { return kuota; }
    public void setKuota(Integer kuota) { this.kuota = kuota; }
    
    public Integer getTerpakai() { return terpakai; }
    public void setTerpakai(Integer terpakai) { this.terpakai = terpakai; }
    
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Diskon diskon = (Diskon) o;
        return Objects.equals(id, diskon.id) && Objects.equals(kode, diskon.kode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, kode);
    }
    
    @Override
    public String toString() {
        return "Diskon{" +
                "id=" + id +
                ", kode='" + kode + '\'' +
                ", nama='" + nama + '\'' +
                ", tipe='" + tipe + '\'' +
                ", nilai=" + nilai +
                '}';
    }
}
