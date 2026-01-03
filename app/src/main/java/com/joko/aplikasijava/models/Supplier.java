package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity Supplier - Menyimpan data supplier/pemasok
 */
@Entity
@Table(name = "supplier", indexes = {
    @Index(name = "idx_supplier_kode", columnList = "kode"),
    @Index(name = "idx_supplier_nama", columnList = "nama")
})
public class Supplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Kode supplier tidak boleh kosong")
    @Size(max = 20, message = "Kode supplier maksimal 20 karakter")
    @Column(name = "kode", unique = true, nullable = false, length = 20)
    private String kode;
    
    @NotBlank(message = "Nama supplier tidak boleh kosong")
    @Size(max = 100, message = "Nama supplier maksimal 100 karakter")
    @Column(name = "nama", nullable = false, length = 100)
    private String nama;
    
    @Column(name = "nama_kontak", length = 100)
    private String namaKontak;
    
    @Column(name = "no_telepon", length = 20)
    private String noTelepon;
    
    @Column(name = "no_fax", length = 20)
    private String noFax;
    
    @Email(message = "Format email tidak valid")
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "website", length = 200)
    private String website;
    
    @Column(name = "alamat", columnDefinition = "TEXT")
    private String alamat;
    
    @Column(name = "kota", length = 50)
    private String kota;
    
    @Column(name = "provinsi", length = 50)
    private String provinsi;
    
    @Column(name = "kode_pos", length = 10)
    private String kodePos;
    
    @Column(name = "npwp", length = 30)
    private String npwp;
    
    @Column(name = "no_rekening", length = 30)
    private String noRekening;
    
    @Column(name = "nama_bank", length = 50)
    private String namaBank;
    
    @Column(name = "atas_nama", length = 100)
    private String atasNama;
    
    @Column(name = "termin_pembayaran")
    private Integer terminPembayaran = 0; // dalam hari
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "aktif", nullable = false)
    private Boolean aktif = true;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<Produk> produkList = new ArrayList<>();
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<PembelianSupplier> pembelianList = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Supplier() {}
    
    public Supplier(String kode, String nama) {
        this.kode = kode;
        this.nama = nama;
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
    
    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getNamaKontak() { return namaKontak; }
    public void setNamaKontak(String namaKontak) { this.namaKontak = namaKontak; }
    
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
    
    public String getNoFax() { return noFax; }
    public void setNoFax(String noFax) { this.noFax = noFax; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    
    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }
    
    public String getProvinsi() { return provinsi; }
    public void setProvinsi(String provinsi) { this.provinsi = provinsi; }
    
    public String getKodePos() { return kodePos; }
    public void setKodePos(String kodePos) { this.kodePos = kodePos; }
    
    public String getNpwp() { return npwp; }
    public void setNpwp(String npwp) { this.npwp = npwp; }
    
    public String getNoRekening() { return noRekening; }
    public void setNoRekening(String noRekening) { this.noRekening = noRekening; }
    
    public String getNamaBank() { return namaBank; }
    public void setNamaBank(String namaBank) { this.namaBank = namaBank; }
    
    public String getAtasNama() { return atasNama; }
    public void setAtasNama(String atasNama) { this.atasNama = atasNama; }
    
    public Integer getTerminPembayaran() { return terminPembayaran; }
    public void setTerminPembayaran(Integer terminPembayaran) { this.terminPembayaran = terminPembayaran; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
    
    public List<Produk> getProdukList() { return produkList; }
    public void setProdukList(List<Produk> produkList) { this.produkList = produkList; }
    
    public List<PembelianSupplier> getPembelianList() { return pembelianList; }
    public void setPembelianList(List<PembelianSupplier> pembelianList) { this.pembelianList = pembelianList; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(id, supplier.id) && Objects.equals(kode, supplier.kode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, kode);
    }
    
    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", kode='" + kode + '\'' +
                ", nama='" + nama + '\'' +
                ", noTelepon='" + noTelepon + '\'' +
                '}';
    }
}
