package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity Transaksi - Menyimpan data transaksi penjualan
 * Merupakan master record untuk setiap transaksi
 */
@Entity
@Table(name = "transaksi", indexes = {
    @Index(name = "idx_transaksi_nomor", columnList = "nomor_transaksi"),
    @Index(name = "idx_transaksi_tanggal", columnList = "tanggal_transaksi"),
    @Index(name = "idx_transaksi_kasir", columnList = "kasir_id"),
    @Index(name = "idx_transaksi_pelanggan", columnList = "pelanggan_id")
})
public class Transaksi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nomor transaksi tidak boleh kosong")
    @Column(name = "nomor_transaksi", unique = true, nullable = false, length = 50)
    private String nomorTransaksi;
    
    @Column(name = "nomor_invoice", unique = true, length = 50)
    private String nomorInvoice;
    
    @NotNull(message = "Tanggal transaksi tidak boleh kosong")
    @Column(name = "tanggal_transaksi", nullable = false)
    private LocalDateTime tanggalTransaksi;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kasir_id", nullable = false)
    private User kasir;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pelanggan_id")
    private Pelanggan pelanggan;
    
    @OneToMany(mappedBy = "transaksi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailTransaksi> detailTransaksiList = new ArrayList<>();
    
    @OneToMany(mappedBy = "transaksi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pembayaran> pembayaranList = new ArrayList<>();
    
    @NotNull(message = "Subtotal tidak boleh kosong")
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "diskon_persen", precision = 5, scale = 2)
    private BigDecimal diskonPersen = BigDecimal.ZERO;
    
    @Column(name = "diskon_nominal", precision = 15, scale = 2)
    private BigDecimal diskonNominal = BigDecimal.ZERO;
    
    @Column(name = "ppn_persen", precision = 5, scale = 2)
    private BigDecimal ppnPersen = new BigDecimal("11"); // Default PPN 11%
    
    @Column(name = "ppn_nominal", precision = 15, scale = 2)
    private BigDecimal ppnNominal = BigDecimal.ZERO;
    
    @Column(name = "service_charge", precision = 15, scale = 2)
    private BigDecimal serviceCharge = BigDecimal.ZERO;
    
    @NotNull(message = "Grand total tidak boleh kosong")
    @Column(name = "grand_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;
    
    @Column(name = "total_bayar", precision = 15, scale = 2)
    private BigDecimal totalBayar = BigDecimal.ZERO;
    
    @Column(name = "kembalian", precision = 15, scale = 2)
    private BigDecimal kembalian = BigDecimal.ZERO;
    
    @Column(name = "total_item")
    private Integer totalItem = 0;
    
    @Column(name = "total_qty")
    private Integer totalQty = 0;
    
    @Column(name = "poin_digunakan")
    private Integer poinDigunakan = 0;
    
    @Column(name = "poin_didapat")
    private Integer poinDidapat = 0;
    
    @NotBlank(message = "Status tidak boleh kosong")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, SELESAI, DIBATALKAN, DIKEMBALIKAN
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "alasan_pembatalan", columnDefinition = "TEXT")
    private String alasanPembatalan;
    
    @Column(name = "dibatalkan_oleh")
    private Long dibatalkanOleh;
    
    @Column(name = "tanggal_pembatalan")
    private LocalDateTime tanggalPembatalan;
    
    @Column(name = "no_meja", length = 20)
    private String noMeja;
    
    @Column(name = "tipe_pesanan", length = 20)
    private String tipePesanan = "DINE_IN"; // DINE_IN, TAKE_AWAY, DELIVERY
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Transaksi() {}
    
    public Transaksi(String nomorTransaksi, User kasir) {
        this.nomorTransaksi = nomorTransaksi;
        this.kasir = kasir;
        this.tanggalTransaksi = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (tanggalTransaksi == null) {
            tanggalTransaksi = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public void addDetail(DetailTransaksi detail) {
        detailTransaksiList.add(detail);
        detail.setTransaksi(this);
        recalculate();
    }
    
    public void removeDetail(DetailTransaksi detail) {
        detailTransaksiList.remove(detail);
        detail.setTransaksi(null);
        recalculate();
    }
    
    public void addPembayaran(Pembayaran pembayaran) {
        pembayaranList.add(pembayaran);
        pembayaran.setTransaksi(this);
        calculateTotalBayar();
    }
    
    public void recalculate() {
        // Hitung subtotal
        subtotal = BigDecimal.ZERO;
        totalItem = 0;
        totalQty = 0;
        
        for (DetailTransaksi detail : detailTransaksiList) {
            subtotal = subtotal.add(detail.getSubtotal());
            totalItem++;
            totalQty += detail.getQty();
        }
        
        // Hitung diskon
        BigDecimal totalDiskon = diskonNominal;
        if (diskonPersen != null && diskonPersen.compareTo(BigDecimal.ZERO) > 0) {
            totalDiskon = totalDiskon.add(subtotal.multiply(diskonPersen).divide(new BigDecimal("100")));
        }
        
        // Hitung setelah diskon
        BigDecimal afterDiskon = subtotal.subtract(totalDiskon);
        
        // Hitung PPN
        if (ppnPersen != null && ppnPersen.compareTo(BigDecimal.ZERO) > 0) {
            ppnNominal = afterDiskon.multiply(ppnPersen).divide(new BigDecimal("100"));
        }
        
        // Grand total
        grandTotal = afterDiskon.add(ppnNominal).add(serviceCharge);
        
        // Hitung kembalian
        calculateKembalian();
    }
    
    private void calculateTotalBayar() {
        totalBayar = BigDecimal.ZERO;
        for (Pembayaran p : pembayaranList) {
            totalBayar = totalBayar.add(p.getJumlah());
        }
        calculateKembalian();
    }
    
    private void calculateKembalian() {
        if (totalBayar.compareTo(grandTotal) > 0) {
            kembalian = totalBayar.subtract(grandTotal);
        } else {
            kembalian = BigDecimal.ZERO;
        }
    }
    
    public boolean isLunas() {
        return totalBayar.compareTo(grandTotal) >= 0;
    }
    
    public BigDecimal getSisaBayar() {
        BigDecimal sisa = grandTotal.subtract(totalBayar);
        return sisa.compareTo(BigDecimal.ZERO) > 0 ? sisa : BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNomorTransaksi() { return nomorTransaksi; }
    public void setNomorTransaksi(String nomorTransaksi) { this.nomorTransaksi = nomorTransaksi; }
    
    public String getNomorInvoice() { return nomorInvoice; }
    public void setNomorInvoice(String nomorInvoice) { this.nomorInvoice = nomorInvoice; }
    
    public LocalDateTime getTanggalTransaksi() { return tanggalTransaksi; }
    public void setTanggalTransaksi(LocalDateTime tanggalTransaksi) { this.tanggalTransaksi = tanggalTransaksi; }
    
    public User getKasir() { return kasir; }
    public void setKasir(User kasir) { this.kasir = kasir; }
    
    public Pelanggan getPelanggan() { return pelanggan; }
    public void setPelanggan(Pelanggan pelanggan) { this.pelanggan = pelanggan; }
    
    public List<DetailTransaksi> getDetailTransaksiList() { return detailTransaksiList; }
    public void setDetailTransaksiList(List<DetailTransaksi> detailTransaksiList) { this.detailTransaksiList = detailTransaksiList; }
    
    public List<Pembayaran> getPembayaranList() { return pembayaranList; }
    public void setPembayaranList(List<Pembayaran> pembayaranList) { this.pembayaranList = pembayaranList; }
    
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
    
    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }
    
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    
    public BigDecimal getTotalBayar() { return totalBayar; }
    public void setTotalBayar(BigDecimal totalBayar) { this.totalBayar = totalBayar; }
    
    public BigDecimal getKembalian() { return kembalian; }
    public void setKembalian(BigDecimal kembalian) { this.kembalian = kembalian; }
    
    public Integer getTotalItem() { return totalItem; }
    public void setTotalItem(Integer totalItem) { this.totalItem = totalItem; }
    
    public Integer getTotalQty() { return totalQty; }
    public void setTotalQty(Integer totalQty) { this.totalQty = totalQty; }
    
    public Integer getPoinDigunakan() { return poinDigunakan; }
    public void setPoinDigunakan(Integer poinDigunakan) { this.poinDigunakan = poinDigunakan; }
    
    public Integer getPoinDidapat() { return poinDidapat; }
    public void setPoinDidapat(Integer poinDidapat) { this.poinDidapat = poinDidapat; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public String getAlasanPembatalan() { return alasanPembatalan; }
    public void setAlasanPembatalan(String alasanPembatalan) { this.alasanPembatalan = alasanPembatalan; }
    
    public Long getDibatalkanOleh() { return dibatalkanOleh; }
    public void setDibatalkanOleh(Long dibatalkanOleh) { this.dibatalkanOleh = dibatalkanOleh; }
    
    public LocalDateTime getTanggalPembatalan() { return tanggalPembatalan; }
    public void setTanggalPembatalan(LocalDateTime tanggalPembatalan) { this.tanggalPembatalan = tanggalPembatalan; }
    
    public String getNoMeja() { return noMeja; }
    public void setNoMeja(String noMeja) { this.noMeja = noMeja; }
    
    public String getTipePesanan() { return tipePesanan; }
    public void setTipePesanan(String tipePesanan) { this.tipePesanan = tipePesanan; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaksi transaksi = (Transaksi) o;
        return Objects.equals(id, transaksi.id) && Objects.equals(nomorTransaksi, transaksi.nomorTransaksi);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nomorTransaksi);
    }
    
    @Override
    public String toString() {
        return "Transaksi{" +
                "id=" + id +
                ", nomorTransaksi='" + nomorTransaksi + '\'' +
                ", tanggalTransaksi=" + tanggalTransaksi +
                ", grandTotal=" + grandTotal +
                ", status='" + status + '\'' +
                '}';
    }
}
