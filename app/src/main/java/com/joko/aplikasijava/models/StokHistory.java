package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity StokHistory - Menyimpan riwayat perubahan stok
 * Untuk tracking mutasi stok produk
 */
@Entity
@Table(name = "stok_history", indexes = {
    @Index(name = "idx_stok_produk", columnList = "produk_id"),
    @Index(name = "idx_stok_tanggal", columnList = "tanggal"),
    @Index(name = "idx_stok_tipe", columnList = "tipe")
})
public class StokHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produk_id", nullable = false)
    private Produk produk;
    
    @Column(name = "tipe", nullable = false, length = 20)
    private String tipe; // MASUK, KELUAR, ADJUSTMENT, RETUR, EXPIRED, RUSAK
    
    @Column(name = "qty", nullable = false)
    private Integer qty;
    
    @Column(name = "stok_sebelum", nullable = false)
    private Integer stokSebelum;
    
    @Column(name = "stok_sesudah", nullable = false)
    private Integer stokSesudah;
    
    @Column(name = "referensi_tipe", length = 50)
    private String referensiTipe; // TRANSAKSI, PEMBELIAN, ADJUSTMENT, etc.
    
    @Column(name = "referensi_id")
    private Long referensiId;
    
    @Column(name = "referensi_nomor", length = 50)
    private String referensiNomor;
    
    @Column(name = "keterangan", columnDefinition = "TEXT")
    private String keterangan;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "tanggal", nullable = false)
    private LocalDateTime tanggal;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public StokHistory() {}
    
    public StokHistory(Produk produk, String tipe, Integer qty, Integer stokSebelum, Integer stokSesudah) {
        this.produk = produk;
        this.tipe = tipe;
        this.qty = qty;
        this.stokSebelum = stokSebelum;
        this.stokSesudah = stokSesudah;
        this.tanggal = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (tanggal == null) {
            tanggal = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Produk getProduk() { return produk; }
    public void setProduk(Produk produk) { this.produk = produk; }
    
    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }
    
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    
    public Integer getStokSebelum() { return stokSebelum; }
    public void setStokSebelum(Integer stokSebelum) { this.stokSebelum = stokSebelum; }
    
    public Integer getStokSesudah() { return stokSesudah; }
    public void setStokSesudah(Integer stokSesudah) { this.stokSesudah = stokSesudah; }
    
    public String getReferensiTipe() { return referensiTipe; }
    public void setReferensiTipe(String referensiTipe) { this.referensiTipe = referensiTipe; }
    
    public Long getReferensiId() { return referensiId; }
    public void setReferensiId(Long referensiId) { this.referensiId = referensiId; }
    
    public String getReferensiNomor() { return referensiNomor; }
    public void setReferensiNomor(String referensiNomor) { this.referensiNomor = referensiNomor; }
    
    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDateTime getTanggal() { return tanggal; }
    public void setTanggal(LocalDateTime tanggal) { this.tanggal = tanggal; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StokHistory that = (StokHistory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "StokHistory{" +
                "id=" + id +
                ", tipe='" + tipe + '\'' +
                ", qty=" + qty +
                ", stokSebelum=" + stokSebelum +
                ", stokSesudah=" + stokSesudah +
                '}';
    }
}
