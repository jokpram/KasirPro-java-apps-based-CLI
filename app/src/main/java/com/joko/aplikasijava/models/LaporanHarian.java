package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity LaporanHarian - Menyimpan rekapitulasi laporan harian
 */
@Entity
@Table(name = "laporan_harian", indexes = {
    @Index(name = "idx_laporan_tanggal", columnList = "tanggal", unique = true)
})
public class LaporanHarian {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tanggal", nullable = false, unique = true)
    private LocalDate tanggal;
    
    // Penjualan
    @Column(name = "jumlah_transaksi")
    private Integer jumlahTransaksi = 0;
    
    @Column(name = "jumlah_item_terjual")
    private Integer jumlahItemTerjual = 0;
    
    @Column(name = "total_penjualan", precision = 15, scale = 2)
    private BigDecimal totalPenjualan = BigDecimal.ZERO;
    
    @Column(name = "total_diskon", precision = 15, scale = 2)
    private BigDecimal totalDiskon = BigDecimal.ZERO;
    
    @Column(name = "total_ppn", precision = 15, scale = 2)
    private BigDecimal totalPpn = BigDecimal.ZERO;
    
    @Column(name = "total_bersih", precision = 15, scale = 2)
    private BigDecimal totalBersih = BigDecimal.ZERO;
    
    // Pembayaran per metode
    @Column(name = "tunai", precision = 15, scale = 2)
    private BigDecimal tunai = BigDecimal.ZERO;
    
    @Column(name = "debit", precision = 15, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;
    
    @Column(name = "kredit", precision = 15, scale = 2)
    private BigDecimal kredit = BigDecimal.ZERO;
    
    @Column(name = "qris", precision = 15, scale = 2)
    private BigDecimal qris = BigDecimal.ZERO;
    
    @Column(name = "transfer", precision = 15, scale = 2)
    private BigDecimal transfer = BigDecimal.ZERO;
    
    // Modal & Profit
    @Column(name = "total_modal", precision = 15, scale = 2)
    private BigDecimal totalModal = BigDecimal.ZERO;
    
    @Column(name = "laba_kotor", precision = 15, scale = 2)
    private BigDecimal labaKotor = BigDecimal.ZERO;
    
    // Pembatalan & Retur
    @Column(name = "jumlah_pembatalan")
    private Integer jumlahPembatalan = 0;
    
    @Column(name = "nilai_pembatalan", precision = 15, scale = 2)
    private BigDecimal nilaiPembatalan = BigDecimal.ZERO;
    
    @Column(name = "jumlah_retur")
    private Integer jumlahRetur = 0;
    
    @Column(name = "nilai_retur", precision = 15, scale = 2)
    private BigDecimal nilaiRetur = BigDecimal.ZERO;
    
    // Customer
    @Column(name = "pelanggan_baru")
    private Integer pelangganBaru = 0;
    
    @Column(name = "pelanggan_member")
    private Integer pelangganMember = 0;
    
    @Column(name = "pelanggan_umum")
    private Integer pelangganUmum = 0;
    
    // Kas
    @Column(name = "saldo_awal", precision = 15, scale = 2)
    private BigDecimal saldoAwal = BigDecimal.ZERO;
    
    @Column(name = "kas_masuk", precision = 15, scale = 2)
    private BigDecimal kasMasuk = BigDecimal.ZERO;
    
    @Column(name = "kas_keluar", precision = 15, scale = 2)
    private BigDecimal kasKeluar = BigDecimal.ZERO;
    
    @Column(name = "saldo_akhir", precision = 15, scale = 2)
    private BigDecimal saldoAkhir = BigDecimal.ZERO;
    
    @Column(name = "status", length = 20)
    private String status = "OPEN"; // OPEN, CLOSED
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public LaporanHarian() {}
    
    public LaporanHarian(LocalDate tanggal) {
        this.tanggal = tanggal;
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
    public void updateTotals() {
        labaKotor = totalBersih.subtract(totalModal);
        saldoAkhir = saldoAwal.add(kasMasuk).subtract(kasKeluar);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    
    public Integer getJumlahTransaksi() { return jumlahTransaksi; }
    public void setJumlahTransaksi(Integer jumlahTransaksi) { this.jumlahTransaksi = jumlahTransaksi; }
    
    public Integer getJumlahItemTerjual() { return jumlahItemTerjual; }
    public void setJumlahItemTerjual(Integer jumlahItemTerjual) { this.jumlahItemTerjual = jumlahItemTerjual; }
    
    public BigDecimal getTotalPenjualan() { return totalPenjualan; }
    public void setTotalPenjualan(BigDecimal totalPenjualan) { this.totalPenjualan = totalPenjualan; }
    
    public BigDecimal getTotalDiskon() { return totalDiskon; }
    public void setTotalDiskon(BigDecimal totalDiskon) { this.totalDiskon = totalDiskon; }
    
    public BigDecimal getTotalPpn() { return totalPpn; }
    public void setTotalPpn(BigDecimal totalPpn) { this.totalPpn = totalPpn; }
    
    public BigDecimal getTotalBersih() { return totalBersih; }
    public void setTotalBersih(BigDecimal totalBersih) { this.totalBersih = totalBersih; }
    
    public BigDecimal getTunai() { return tunai; }
    public void setTunai(BigDecimal tunai) { this.tunai = tunai; }
    
    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }
    
    public BigDecimal getKredit() { return kredit; }
    public void setKredit(BigDecimal kredit) { this.kredit = kredit; }
    
    public BigDecimal getQris() { return qris; }
    public void setQris(BigDecimal qris) { this.qris = qris; }
    
    public BigDecimal getTransfer() { return transfer; }
    public void setTransfer(BigDecimal transfer) { this.transfer = transfer; }
    
    public BigDecimal getTotalModal() { return totalModal; }
    public void setTotalModal(BigDecimal totalModal) { this.totalModal = totalModal; }
    
    public BigDecimal getLabaKotor() { return labaKotor; }
    public void setLabaKotor(BigDecimal labaKotor) { this.labaKotor = labaKotor; }
    
    public Integer getJumlahPembatalan() { return jumlahPembatalan; }
    public void setJumlahPembatalan(Integer jumlahPembatalan) { this.jumlahPembatalan = jumlahPembatalan; }
    
    public BigDecimal getNilaiPembatalan() { return nilaiPembatalan; }
    public void setNilaiPembatalan(BigDecimal nilaiPembatalan) { this.nilaiPembatalan = nilaiPembatalan; }
    
    public Integer getJumlahRetur() { return jumlahRetur; }
    public void setJumlahRetur(Integer jumlahRetur) { this.jumlahRetur = jumlahRetur; }
    
    public BigDecimal getNilaiRetur() { return nilaiRetur; }
    public void setNilaiRetur(BigDecimal nilaiRetur) { this.nilaiRetur = nilaiRetur; }
    
    public Integer getPelangganBaru() { return pelangganBaru; }
    public void setPelangganBaru(Integer pelangganBaru) { this.pelangganBaru = pelangganBaru; }
    
    public Integer getPelangganMember() { return pelangganMember; }
    public void setPelangganMember(Integer pelangganMember) { this.pelangganMember = pelangganMember; }
    
    public Integer getPelangganUmum() { return pelangganUmum; }
    public void setPelangganUmum(Integer pelangganUmum) { this.pelangganUmum = pelangganUmum; }
    
    public BigDecimal getSaldoAwal() { return saldoAwal; }
    public void setSaldoAwal(BigDecimal saldoAwal) { this.saldoAwal = saldoAwal; }
    
    public BigDecimal getKasMasuk() { return kasMasuk; }
    public void setKasMasuk(BigDecimal kasMasuk) { this.kasMasuk = kasMasuk; }
    
    public BigDecimal getKasKeluar() { return kasKeluar; }
    public void setKasKeluar(BigDecimal kasKeluar) { this.kasKeluar = kasKeluar; }
    
    public BigDecimal getSaldoAkhir() { return saldoAkhir; }
    public void setSaldoAkhir(BigDecimal saldoAkhir) { this.saldoAkhir = saldoAkhir; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
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
        LaporanHarian that = (LaporanHarian) o;
        return Objects.equals(id, that.id) && Objects.equals(tanggal, that.tanggal);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, tanggal);
    }
    
    @Override
    public String toString() {
        return "LaporanHarian{" +
                "tanggal=" + tanggal +
                ", jumlahTransaksi=" + jumlahTransaksi +
                ", totalPenjualan=" + totalPenjualan +
                ", labaKotor=" + labaKotor +
                '}';
    }
}
