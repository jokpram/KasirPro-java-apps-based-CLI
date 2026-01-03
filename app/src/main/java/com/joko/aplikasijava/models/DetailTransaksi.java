package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity DetailTransaksi - Menyimpan detail item dalam transaksi
 * Berisi informasi produk, qty, harga, dan diskon per item
 */
@Entity
@Table(name = "detail_transaksi", indexes = {
    @Index(name = "idx_detail_transaksi", columnList = "transaksi_id"),
    @Index(name = "idx_detail_produk", columnList = "produk_id")
})
public class DetailTransaksi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaksi_id", nullable = false)
    private Transaksi transaksi;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produk_id", nullable = false)
    private Produk produk;
    
    @Column(name = "kode_produk", length = 50)
    private String kodeProduk;
    
    @Column(name = "nama_produk", length = 200)
    private String namaProduk;
    
    @NotNull(message = "Qty tidak boleh kosong")
    @Min(value = 1, message = "Qty minimal 1")
    @Column(name = "qty", nullable = false)
    private Integer qty;
    
    @Column(name = "satuan", length = 20)
    private String satuan;
    
    @NotNull(message = "Harga satuan tidak boleh kosong")
    @Column(name = "harga_satuan", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaSatuan;
    
    @Column(name = "harga_modal", precision = 15, scale = 2)
    private BigDecimal hargaModal;
    
    @Column(name = "diskon_persen", precision = 5, scale = 2)
    private BigDecimal diskonPersen = BigDecimal.ZERO;
    
    @Column(name = "diskon_nominal", precision = 15, scale = 2)
    private BigDecimal diskonNominal = BigDecimal.ZERO;
    
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "ppn", nullable = false)
    private Boolean ppn = true;
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public DetailTransaksi() {}
    
    public DetailTransaksi(Produk produk, Integer qty) {
        this.produk = produk;
        this.kodeProduk = produk.getKode();
        this.namaProduk = produk.getNama();
        this.qty = qty;
        this.hargaSatuan = produk.getHargaJual();
        this.hargaModal = produk.getHargaBeli();
        this.satuan = produk.getSatuan();
        this.ppn = produk.getPpn();
        this.diskonPersen = produk.getDiskonPersen();
        calculateSubtotal();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateSubtotal();
    }
    
    // Business methods
    public void calculateSubtotal() {
        BigDecimal total = hargaSatuan.multiply(new BigDecimal(qty));
        
        // Apply diskon persen
        if (diskonPersen != null && diskonPersen.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diskon = total.multiply(diskonPersen).divide(new BigDecimal("100"));
            total = total.subtract(diskon);
        }
        
        // Apply diskon nominal
        if (diskonNominal != null && diskonNominal.compareTo(BigDecimal.ZERO) > 0) {
            total = total.subtract(diskonNominal);
        }
        
        subtotal = total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getProfit() {
        if (hargaModal == null) return BigDecimal.ZERO;
        BigDecimal modalTotal = hargaModal.multiply(new BigDecimal(qty));
        return subtotal.subtract(modalTotal);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Transaksi getTransaksi() { return transaksi; }
    public void setTransaksi(Transaksi transaksi) { this.transaksi = transaksi; }
    
    public Produk getProduk() { return produk; }
    public void setProduk(Produk produk) { 
        this.produk = produk;
        if (produk != null) {
            this.kodeProduk = produk.getKode();
            this.namaProduk = produk.getNama();
            this.hargaSatuan = produk.getHargaJual();
            this.hargaModal = produk.getHargaBeli();
            this.satuan = produk.getSatuan();
            this.ppn = produk.getPpn();
        }
    }
    
    public String getKodeProduk() { return kodeProduk; }
    public void setKodeProduk(String kodeProduk) { this.kodeProduk = kodeProduk; }
    
    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }
    
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { 
        this.qty = qty; 
        calculateSubtotal();
    }
    
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    
    public BigDecimal getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(BigDecimal hargaSatuan) { 
        this.hargaSatuan = hargaSatuan;
        calculateSubtotal();
    }
    
    public BigDecimal getHargaModal() { return hargaModal; }
    public void setHargaModal(BigDecimal hargaModal) { this.hargaModal = hargaModal; }
    
    public BigDecimal getDiskonPersen() { return diskonPersen; }
    public void setDiskonPersen(BigDecimal diskonPersen) { 
        this.diskonPersen = diskonPersen;
        calculateSubtotal();
    }
    
    public BigDecimal getDiskonNominal() { return diskonNominal; }
    public void setDiskonNominal(BigDecimal diskonNominal) { 
        this.diskonNominal = diskonNominal;
        calculateSubtotal();
    }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public Boolean getPpn() { return ppn; }
    public void setPpn(Boolean ppn) { this.ppn = ppn; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailTransaksi that = (DetailTransaksi) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DetailTransaksi{" +
                "id=" + id +
                ", namaProduk='" + namaProduk + '\'' +
                ", qty=" + qty +
                ", hargaSatuan=" + hargaSatuan +
                ", subtotal=" + subtotal +
                '}';
    }
}
