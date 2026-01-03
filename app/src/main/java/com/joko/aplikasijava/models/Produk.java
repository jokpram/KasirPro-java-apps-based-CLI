package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity Produk - Menyimpan data produk yang dijual
 * Termasuk informasi harga, stok, dan metadata produk
 */
@Entity
@Table(name = "produk", indexes = {
    @Index(name = "idx_produk_kode", columnList = "kode"),
    @Index(name = "idx_produk_barcode", columnList = "barcode"),
    @Index(name = "idx_produk_nama", columnList = "nama"),
    @Index(name = "idx_produk_kategori", columnList = "kategori_id")
})
public class Produk {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Kode produk tidak boleh kosong")
    @Size(max = 50, message = "Kode produk maksimal 50 karakter")
    @Column(name = "kode", unique = true, nullable = false, length = 50)
    private String kode;
    
    @Column(name = "barcode", unique = true, length = 50)
    private String barcode;
    
    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Size(max = 200, message = "Nama produk maksimal 200 karakter")
    @Column(name = "nama", nullable = false, length = 200)
    private String nama;
    
    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategori_id")
    private Kategori kategori;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @NotNull(message = "Harga beli tidak boleh kosong")
    @DecimalMin(value = "0.0", message = "Harga beli tidak boleh negatif")
    @Column(name = "harga_beli", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaBeli;
    
    @NotNull(message = "Harga jual tidak boleh kosong")
    @DecimalMin(value = "0.0", message = "Harga jual tidak boleh negatif")
    @Column(name = "harga_jual", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaJual;
    
    @Column(name = "harga_grosir", precision = 15, scale = 2)
    private BigDecimal hargaGrosir;
    
    @Column(name = "min_grosir")
    private Integer minGrosir;
    
    @NotNull(message = "Stok tidak boleh kosong")
    @Min(value = 0, message = "Stok tidak boleh negatif")
    @Column(name = "stok", nullable = false)
    private Integer stok = 0;
    
    @Column(name = "stok_minimum")
    private Integer stokMinimum = 10;
    
    @NotBlank(message = "Satuan tidak boleh kosong")
    @Size(max = 20, message = "Satuan maksimal 20 karakter")
    @Column(name = "satuan", nullable = false, length = 20)
    private String satuan; // PCS, KG, LITER, BOX, etc.
    
    @Column(name = "berat")
    private Double berat; // dalam gram
    
    @Column(name = "dimensi", length = 50)
    private String dimensi; // P x L x T dalam cm
    
    @Column(name = "gambar")
    private String gambar;
    
    @Column(name = "rak", length = 50)
    private String rak; // Lokasi rak di toko
    
    @Column(name = "tanggal_kadaluarsa")
    private LocalDateTime tanggalKadaluarsa;
    
    @Column(name = "terjual")
    private Integer terjual = 0; // Total terjual
    
    @DecimalMin(value = "0.0", message = "Diskon tidak boleh negatif")
    @DecimalMax(value = "100.0", message = "Diskon maksimal 100%")
    @Column(name = "diskon_persen", precision = 5, scale = 2)
    private BigDecimal diskonPersen = BigDecimal.ZERO;
    
    @Column(name = "ppn", nullable = false)
    private Boolean ppn = true; // Apakah kena PPN
    
    @Column(name = "aktif", nullable = false)
    private Boolean aktif = true;
    
    @Column(name = "favorit", nullable = false)
    private Boolean favorit = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    // Constructors
    public Produk() {}
    
    public Produk(String kode, String nama, BigDecimal hargaBeli, BigDecimal hargaJual, Integer stok, String satuan) {
        this.kode = kode;
        this.nama = nama;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
        this.satuan = satuan;
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
    public BigDecimal getHargaSetelahDiskon() {
        if (diskonPersen == null || diskonPersen.compareTo(BigDecimal.ZERO) == 0) {
            return hargaJual;
        }
        BigDecimal diskon = hargaJual.multiply(diskonPersen).divide(new BigDecimal("100"));
        return hargaJual.subtract(diskon);
    }
    
    public BigDecimal getMargin() {
        return hargaJual.subtract(hargaBeli);
    }
    
    public BigDecimal getMarginPersen() {
        if (hargaBeli.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getMargin().multiply(new BigDecimal("100")).divide(hargaBeli, 2, java.math.RoundingMode.HALF_UP);
    }
    
    public boolean isLowStock() {
        return stok <= stokMinimum;
    }
    
    public boolean isOutOfStock() {
        return stok <= 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    public Kategori getKategori() { return kategori; }
    public void setKategori(Kategori kategori) { this.kategori = kategori; }
    
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    
    public BigDecimal getHargaBeli() { return hargaBeli; }
    public void setHargaBeli(BigDecimal hargaBeli) { this.hargaBeli = hargaBeli; }
    
    public BigDecimal getHargaJual() { return hargaJual; }
    public void setHargaJual(BigDecimal hargaJual) { this.hargaJual = hargaJual; }
    
    public BigDecimal getHargaGrosir() { return hargaGrosir; }
    public void setHargaGrosir(BigDecimal hargaGrosir) { this.hargaGrosir = hargaGrosir; }
    
    public Integer getMinGrosir() { return minGrosir; }
    public void setMinGrosir(Integer minGrosir) { this.minGrosir = minGrosir; }
    
    public Integer getStok() { return stok; }
    public void setStok(Integer stok) { this.stok = stok; }
    
    public Integer getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(Integer stokMinimum) { this.stokMinimum = stokMinimum; }
    
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    
    public Double getBerat() { return berat; }
    public void setBerat(Double berat) { this.berat = berat; }
    
    public String getDimensi() { return dimensi; }
    public void setDimensi(String dimensi) { this.dimensi = dimensi; }
    
    public String getGambar() { return gambar; }
    public void setGambar(String gambar) { this.gambar = gambar; }
    
    public String getRak() { return rak; }
    public void setRak(String rak) { this.rak = rak; }
    
    public LocalDateTime getTanggalKadaluarsa() { return tanggalKadaluarsa; }
    public void setTanggalKadaluarsa(LocalDateTime tanggalKadaluarsa) { this.tanggalKadaluarsa = tanggalKadaluarsa; }
    
    public Integer getTerjual() { return terjual; }
    public void setTerjual(Integer terjual) { this.terjual = terjual; }
    
    public BigDecimal getDiskonPersen() { return diskonPersen; }
    public void setDiskonPersen(BigDecimal diskonPersen) { this.diskonPersen = diskonPersen; }
    
    public Boolean getPpn() { return ppn; }
    public void setPpn(Boolean ppn) { this.ppn = ppn; }
    
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
    
    public Boolean getFavorit() { return favorit; }
    public void setFavorit(Boolean favorit) { this.favorit = favorit; }
    
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
        Produk produk = (Produk) o;
        return Objects.equals(id, produk.id) && Objects.equals(kode, produk.kode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, kode);
    }
    
    @Override
    public String toString() {
        return "Produk{" +
                "id=" + id +
                ", kode='" + kode + '\'' +
                ", nama='" + nama + '\'' +
                ", hargaJual=" + hargaJual +
                ", stok=" + stok +
                ", satuan='" + satuan + '\'' +
                '}';
    }
}
