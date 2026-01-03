package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity Kategori - Menyimpan data kategori produk
 * Mendukung hierarki kategori dengan parent-child relationship
 */
@Entity
@Table(name = "kategori", indexes = {
    @Index(name = "idx_kategori_kode", columnList = "kode"),
    @Index(name = "idx_kategori_nama", columnList = "nama")
})
public class Kategori {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Kode kategori tidak boleh kosong")
    @Size(max = 20, message = "Kode kategori maksimal 20 karakter")
    @Column(name = "kode", unique = true, nullable = false, length = 20)
    private String kode;
    
    @NotBlank(message = "Nama kategori tidak boleh kosong")
    @Size(max = 100, message = "Nama kategori maksimal 100 karakter")
    @Column(name = "nama", nullable = false, length = 100)
    private String nama;
    
    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Kategori parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Kategori> children = new ArrayList<>();
    
    @OneToMany(mappedBy = "kategori", cascade = CascadeType.ALL)
    private List<Produk> produkList = new ArrayList<>();
    
    @Column(name = "icon")
    private String icon;
    
    @Column(name = "warna")
    private String warna;
    
    @Column(name = "urutan")
    private Integer urutan = 0;
    
    @Column(name = "aktif", nullable = false)
    private Boolean aktif = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Kategori() {}
    
    public Kategori(String kode, String nama) {
        this.kode = kode;
        this.nama = nama;
    }
    
    public Kategori(String kode, String nama, String deskripsi) {
        this.kode = kode;
        this.nama = nama;
        this.deskripsi = deskripsi;
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
    
    // Helper methods
    public void addChild(Kategori child) {
        children.add(child);
        child.setParent(this);
    }
    
    public void removeChild(Kategori child) {
        children.remove(child);
        child.setParent(null);
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
    
    public Kategori getParent() { return parent; }
    public void setParent(Kategori parent) { this.parent = parent; }
    
    public List<Kategori> getChildren() { return children; }
    public void setChildren(List<Kategori> children) { this.children = children; }
    
    public List<Produk> getProdukList() { return produkList; }
    public void setProdukList(List<Produk> produkList) { this.produkList = produkList; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public String getWarna() { return warna; }
    public void setWarna(String warna) { this.warna = warna; }
    
    public Integer getUrutan() { return urutan; }
    public void setUrutan(Integer urutan) { this.urutan = urutan; }
    
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
        Kategori kategori = (Kategori) o;
        return Objects.equals(id, kategori.id) && Objects.equals(kode, kategori.kode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, kode);
    }
    
    @Override
    public String toString() {
        return "Kategori{" +
                "id=" + id +
                ", kode='" + kode + '\'' +
                ", nama='" + nama + '\'' +
                ", aktif=" + aktif +
                '}';
    }
}
