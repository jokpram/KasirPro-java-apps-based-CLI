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
 * Entity PembelianSupplier - Menyimpan data pembelian dari supplier
 */
@Entity
@Table(name = "pembelian_supplier", indexes = {
    @Index(name = "idx_pembelian_nomor", columnList = "nomor_po"),
    @Index(name = "idx_pembelian_supplier", columnList = "supplier_id"),
    @Index(name = "idx_pembelian_tanggal", columnList = "tanggal_pembelian")
})
public class PembelianSupplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nomor PO tidak boleh kosong")
    @Column(name = "nomor_po", unique = true, nullable = false, length = 50)
    private String nomorPO;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    
    @NotNull(message = "Tanggal pembelian tidak boleh kosong")
    @Column(name = "tanggal_pembelian", nullable = false)
    private LocalDate tanggalPembelian;
    
    @Column(name = "tanggal_diterima")
    private LocalDate tanggalDiterima;
    
    @Column(name = "tanggal_jatuh_tempo")
    private LocalDate tanggalJatuhTempo;
    
    @OneToMany(mappedBy = "pembelian", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailPembelian> detailList = new ArrayList<>();
    
    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "diskon_persen", precision = 5, scale = 2)
    private BigDecimal diskonPersen = BigDecimal.ZERO;
    
    @Column(name = "diskon_nominal", precision = 15, scale = 2)
    private BigDecimal diskonNominal = BigDecimal.ZERO;
    
    @Column(name = "ppn_persen", precision = 5, scale = 2)
    private BigDecimal ppnPersen = BigDecimal.ZERO;
    
    @Column(name = "ppn_nominal", precision = 15, scale = 2)
    private BigDecimal ppnNominal = BigDecimal.ZERO;
    
    @Column(name = "biaya_kirim", precision = 15, scale = 2)
    private BigDecimal biayaKirim = BigDecimal.ZERO;
    
    @Column(name = "biaya_lain", precision = 15, scale = 2)
    private BigDecimal biayaLain = BigDecimal.ZERO;
    
    @Column(name = "grand_total", precision = 15, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;
    
    @Column(name = "total_bayar", precision = 15, scale = 2)
    private BigDecimal totalBayar = BigDecimal.ZERO;
    
    @Column(name = "sisa_bayar", precision = 15, scale = 2)
    private BigDecimal sisaBayar = BigDecimal.ZERO;
    
    @Column(name = "status", length = 20)
    private String status = "DRAFT"; // DRAFT, DIPESAN, DITERIMA, SEBAGIAN, SELESAI, DIBATALKAN
    
    @Column(name = "status_bayar", length = 20)
    private String statusBayar = "BELUM_LUNAS"; // BELUM_LUNAS, SEBAGIAN, LUNAS
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PembelianSupplier() {}
    
    public PembelianSupplier(String nomorPO, Supplier supplier) {
        this.nomorPO = nomorPO;
        this.supplier = supplier;
        this.tanggalPembelian = LocalDate.now();
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
    public void addDetail(DetailPembelian detail) {
        detailList.add(detail);
        detail.setPembelian(this);
        recalculate();
    }
    
    public void removeDetail(DetailPembelian detail) {
        detailList.remove(detail);
        detail.setPembelian(null);
        recalculate();
    }
    
    public void recalculate() {
        subtotal = BigDecimal.ZERO;
        for (DetailPembelian detail : detailList) {
            subtotal = subtotal.add(detail.getSubtotal());
        }
        
        BigDecimal totalDiskon = diskonNominal;
        if (diskonPersen != null && diskonPersen.compareTo(BigDecimal.ZERO) > 0) {
            totalDiskon = totalDiskon.add(subtotal.multiply(diskonPersen).divide(new BigDecimal("100")));
        }
        
        BigDecimal afterDiskon = subtotal.subtract(totalDiskon);
        
        if (ppnPersen != null && ppnPersen.compareTo(BigDecimal.ZERO) > 0) {
            ppnNominal = afterDiskon.multiply(ppnPersen).divide(new BigDecimal("100"));
        }
        
        grandTotal = afterDiskon.add(ppnNominal).add(biayaKirim).add(biayaLain);
        sisaBayar = grandTotal.subtract(totalBayar);
        
        updateStatusBayar();
    }
    
    private void updateStatusBayar() {
        if (totalBayar.compareTo(BigDecimal.ZERO) == 0) {
            statusBayar = "BELUM_LUNAS";
        } else if (totalBayar.compareTo(grandTotal) >= 0) {
            statusBayar = "LUNAS";
        } else {
            statusBayar = "SEBAGIAN";
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNomorPO() { return nomorPO; }
    public void setNomorPO(String nomorPO) { this.nomorPO = nomorPO; }
    
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    
    public LocalDate getTanggalPembelian() { return tanggalPembelian; }
    public void setTanggalPembelian(LocalDate tanggalPembelian) { this.tanggalPembelian = tanggalPembelian; }
    
    public LocalDate getTanggalDiterima() { return tanggalDiterima; }
    public void setTanggalDiterima(LocalDate tanggalDiterima) { this.tanggalDiterima = tanggalDiterima; }
    
    public LocalDate getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public void setTanggalJatuhTempo(LocalDate tanggalJatuhTempo) { this.tanggalJatuhTempo = tanggalJatuhTempo; }
    
    public List<DetailPembelian> getDetailList() { return detailList; }
    public void setDetailList(List<DetailPembelian> detailList) { this.detailList = detailList; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getDiskonPersen() { return diskonPersen; }
    public void setDiskonPersen(BigDecimal diskonPersen) { this.diskonPersen = diskonPersen; }
    
    public BigDecimal getDiskonNominal() { return diskonNominal; }
    public void setDiskonNominal(BigDecimal diskonNominal) { this.diskonNominal = diskonNominal; }
    
    public BigDecimal getPpnPersen() { return ppnPersen; }
    public void setPpnPersen(BigDecimal ppnPersen) { this.ppnPersen = ppnPersen; }
    
    public BigDecimal getPpnNominal() { return ppnNominal; }
    public void setPpnNominal(BigDecimal ppnNominal) { this.ppnNominal = ppnNominal; }
    
    public BigDecimal getBiayaKirim() { return biayaKirim; }
    public void setBiayaKirim(BigDecimal biayaKirim) { this.biayaKirim = biayaKirim; }
    
    public BigDecimal getBiayaLain() { return biayaLain; }
    public void setBiayaLain(BigDecimal biayaLain) { this.biayaLain = biayaLain; }
    
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    
    public BigDecimal getTotalBayar() { return totalBayar; }
    public void setTotalBayar(BigDecimal totalBayar) { this.totalBayar = totalBayar; }
    
    public BigDecimal getSisaBayar() { return sisaBayar; }
    public void setSisaBayar(BigDecimal sisaBayar) { this.sisaBayar = sisaBayar; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStatusBayar() { return statusBayar; }
    public void setStatusBayar(String statusBayar) { this.statusBayar = statusBayar; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PembelianSupplier that = (PembelianSupplier) o;
        return Objects.equals(id, that.id) && Objects.equals(nomorPO, that.nomorPO);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nomorPO);
    }
    
    @Override
    public String toString() {
        return "PembelianSupplier{" +
                "id=" + id +
                ", nomorPO='" + nomorPO + '\'' +
                ", grandTotal=" + grandTotal +
                ", status='" + status + '\'' +
                '}';
    }
}
