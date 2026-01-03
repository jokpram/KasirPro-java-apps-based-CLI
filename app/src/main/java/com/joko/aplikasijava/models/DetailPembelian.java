package com.joko.aplikasijava.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity DetailPembelian - Menyimpan detail item pembelian
 */
@Entity
@Table(name = "detail_pembelian", indexes = {
    @Index(name = "idx_detail_pembelian", columnList = "pembelian_id"),
    @Index(name = "idx_detail_pembelian_produk", columnList = "produk_id")
})
public class DetailPembelian {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pembelian_id", nullable = false)
    private PembelianSupplier pembelian;
    
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
    
    @Column(name = "qty_diterima")
    private Integer qtyDiterima = 0;
    
    @Column(name = "satuan", length = 20)
    private String satuan;
    
    @NotNull(message = "Harga satuan tidak boleh kosong")
    @Column(name = "harga_satuan", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaSatuan;
    
    @Column(name = "diskon_persen", precision = 5, scale = 2)
    private BigDecimal diskonPersen = BigDecimal.ZERO;
    
    @Column(name = "diskon_nominal", precision = 15, scale = 2)
    private BigDecimal diskonNominal = BigDecimal.ZERO;
    
    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "catatan", columnDefinition = "TEXT")
    private String catatan;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public DetailPembelian() {}
    
    public DetailPembelian(Produk produk, Integer qty, BigDecimal hargaSatuan) {
        this.produk = produk;
        this.kodeProduk = produk.getKode();
        this.namaProduk = produk.getNama();
        this.qty = qty;
        this.hargaSatuan = hargaSatuan;
        this.satuan = produk.getSatuan();
        calculateSubtotal();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public void calculateSubtotal() {
        BigDecimal total = hargaSatuan.multiply(new BigDecimal(qty));
        
        if (diskonPersen != null && diskonPersen.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diskon = total.multiply(diskonPersen).divide(new BigDecimal("100"));
            total = total.subtract(diskon);
        }
        
        if (diskonNominal != null && diskonNominal.compareTo(BigDecimal.ZERO) > 0) {
            total = total.subtract(diskonNominal);
        }
        
        subtotal = total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public PembelianSupplier getPembelian() { return pembelian; }
    public void setPembelian(PembelianSupplier pembelian) { this.pembelian = pembelian; }
    
    public Produk getProduk() { return produk; }
    public void setProduk(Produk produk) { this.produk = produk; }
    
    public String getKodeProduk() { return kodeProduk; }
    public void setKodeProduk(String kodeProduk) { this.kodeProduk = kodeProduk; }
    
    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }
    
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; calculateSubtotal(); }
    
    public Integer getQtyDiterima() { return qtyDiterima; }
    public void setQtyDiterima(Integer qtyDiterima) { this.qtyDiterima = qtyDiterima; }
    
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    
    public BigDecimal getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(BigDecimal hargaSatuan) { this.hargaSatuan = hargaSatuan; calculateSubtotal(); }
    
    public BigDecimal getDiskonPersen() { return diskonPersen; }
    public void setDiskonPersen(BigDecimal diskonPersen) { this.diskonPersen = diskonPersen; calculateSubtotal(); }
    
    public BigDecimal getDiskonNominal() { return diskonNominal; }
    public void setDiskonNominal(BigDecimal diskonNominal) { this.diskonNominal = diskonNominal; calculateSubtotal(); }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailPembelian that = (DetailPembelian) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DetailPembelian{" +
                "id=" + id +
                ", namaProduk='" + namaProduk + '\'' +
                ", qty=" + qty +
                ", hargaSatuan=" + hargaSatuan +
                '}';
    }
}
