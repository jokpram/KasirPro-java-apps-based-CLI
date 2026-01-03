package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity Pembayaran - Menyimpan data pembayaran untuk transaksi
 * Mendukung split payment dengan berbagai metode pembayaran
 */
@Entity
@Table(name = "pembayaran", indexes = {
    @Index(name = "idx_pembayaran_transaksi", columnList = "transaksi_id"),
    @Index(name = "idx_pembayaran_metode", columnList = "metode_pembayaran")
})
public class Pembayaran {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaksi_id", nullable = false)
    private Transaksi transaksi;
    
    @NotBlank(message = "Metode pembayaran tidak boleh kosong")
    @Column(name = "metode_pembayaran", nullable = false, length = 20)
    private String metodePembayaran; // TUNAI, DEBIT, KREDIT, QRIS, TRANSFER
    
    @NotNull(message = "Jumlah pembayaran tidak boleh kosong")
    @DecimalMin(value = "0.01", message = "Jumlah pembayaran minimal 0.01")
    @Column(name = "jumlah", nullable = false, precision = 15, scale = 2)
    private BigDecimal jumlah;
    
    @Column(name = "no_referensi", length = 100)
    private String noReferensi; // Nomor kartu, nomor transfer, dll
    
    @Column(name = "nama_bank", length = 50)
    private String namaBank;
    
    @Column(name = "no_kartu", length = 20)
    private String noKartu; // 4 digit terakhir saja
    
    @Column(name = "nama_pemilik", length = 100)
    private String namaPemilik;
    
    @Column(name = "approval_code", length = 50)
    private String approvalCode;
    
    @Column(name = "biaya_admin", precision = 15, scale = 2)
    private BigDecimal biayaAdmin = BigDecimal.ZERO;
    
    @Column(name = "status", length = 20)
    private String status = "SUKSES"; // SUKSES, GAGAL, PENDING
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "tanggal_bayar", nullable = false)
    private LocalDateTime tanggalBayar;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Pembayaran() {}
    
    public Pembayaran(String metodePembayaran, BigDecimal jumlah) {
        this.metodePembayaran = metodePembayaran;
        this.jumlah = jumlah;
        this.tanggalBayar = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (tanggalBayar == null) {
            tanggalBayar = LocalDateTime.now();
        }
    }
    
    // Business methods
    public BigDecimal getJumlahBersih() {
        return jumlah.subtract(biayaAdmin != null ? biayaAdmin : BigDecimal.ZERO);
    }
    
    public boolean isCash() {
        return "TUNAI".equalsIgnoreCase(metodePembayaran);
    }
    
    public boolean isCard() {
        return "DEBIT".equalsIgnoreCase(metodePembayaran) || "KREDIT".equalsIgnoreCase(metodePembayaran);
    }
    
    public boolean isDigital() {
        return "QRIS".equalsIgnoreCase(metodePembayaran) || "TRANSFER".equalsIgnoreCase(metodePembayaran);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Transaksi getTransaksi() { return transaksi; }
    public void setTransaksi(Transaksi transaksi) { this.transaksi = transaksi; }
    
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    
    public BigDecimal getJumlah() { return jumlah; }
    public void setJumlah(BigDecimal jumlah) { this.jumlah = jumlah; }
    
    public String getNoReferensi() { return noReferensi; }
    public void setNoReferensi(String noReferensi) { this.noReferensi = noReferensi; }
    
    public String getNamaBank() { return namaBank; }
    public void setNamaBank(String namaBank) { this.namaBank = namaBank; }
    
    public String getNoKartu() { return noKartu; }
    public void setNoKartu(String noKartu) { this.noKartu = noKartu; }
    
    public String getNamaPemilik() { return namaPemilik; }
    public void setNamaPemilik(String namaPemilik) { this.namaPemilik = namaPemilik; }
    
    public String getApprovalCode() { return approvalCode; }
    public void setApprovalCode(String approvalCode) { this.approvalCode = approvalCode; }
    
    public BigDecimal getBiayaAdmin() { return biayaAdmin; }
    public void setBiayaAdmin(BigDecimal biayaAdmin) { this.biayaAdmin = biayaAdmin; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public LocalDateTime getTanggalBayar() { return tanggalBayar; }
    public void setTanggalBayar(LocalDateTime tanggalBayar) { this.tanggalBayar = tanggalBayar; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pembayaran that = (Pembayaran) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Pembayaran{" +
                "id=" + id +
                ", metodePembayaran='" + metodePembayaran + '\'' +
                ", jumlah=" + jumlah +
                ", status='" + status + '\'' +
                '}';
    }
}
