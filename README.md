# KASIR PRO - Sistem Kasir Modern

Sistem kasir/POS (Point of Sale) lengkap berbasis Java dengan ORM Hibernate dan PostgreSQL.

##  Fitur Utama

###  Transaksi
- Transaksi penjualan dengan keranjang belanja
- Dukungan multiple item dalam satu transaksi
- Pencarian produk dengan kode atau barcode
- Kalkulasi otomatis subtotal, diskon, PPN, dan kembalian
- Multiple metode pembayaran (Tunai, Debit, Kredit, QRIS, Transfer)
- Cetak struk transaksi
- Void/batalkan transaksi

###  Manajemen Produk
- CRUD produk lengkap
- Dukungan barcode
- Kategori produk dengan hierarki
- Tracking stok dengan history
- Alert stok rendah dan habis
- Produk favorit
- Harga grosir

###  Manajemen Pelanggan
- Sistem membership multi-tier (Regular, Silver, Gold, Platinum)
- Sistem poin rewards
- Diskon otomatis berdasarkan level member
- Tracking history belanja

###  Laporan
- Dashboard ringkasan
- Laporan penjualan harian/periode
- Produk terlaris
- Laporan stok
- Top member

###  Manajemen User
- Multi-role (Admin, Supervisor, Kasir)
- Login dengan password ter-hash (BCrypt)
- Lock user setelah 3x gagal login
- Reset password

##  Arsitektur

```
src/main/java/com/joko/aplikasijava/
├── config/
│   ├── AppConfig.java          # Konstanta aplikasi
│   └── HibernateUtil.java      # Hibernate SessionFactory
├── models/                      # Entity/Model (ORM)
│   ├── User.java
│   ├── Kategori.java
│   ├── Produk.java
│   ├── Pelanggan.java
│   ├── Transaksi.java
│   ├── DetailTransaksi.java
│   ├── Pembayaran.java
│   ├── Supplier.java
│   ├── PembelianSupplier.java
│   ├── DetailPembelian.java
│   ├── StokHistory.java
│   ├── Diskon.java
│   └── LaporanHarian.java
├── repositories/                # Data Access Layer
│   ├── GenericRepository.java
│   ├── UserRepository.java
│   ├── KategoriRepository.java
│   ├── ProdukRepository.java
│   ├── PelangganRepository.java
│   ├── TransaksiRepository.java
│   ├── SupplierRepository.java
│   └── StokHistoryRepository.java
├── services/                    # Business Logic Layer
│   ├── AuthService.java
│   ├── ProdukService.java
│   ├── PelangganService.java
│   ├── TransaksiService.java
│   └── LaporanService.java
├── utils/                       # Utility Classes
│   ├── FormatUtils.java
│   ├── StrukUtils.java
│   └── InputUtils.java
└── App.java                     # Main Application
```

##  Teknologi

- **Java 21** - LTS version
- **Hibernate ORM 6.4** - Object-Relational Mapping
- **PostgreSQL** - Database
- **HikariCP** - Connection Pool
- **BCrypt** - Password hashing
- **SLF4J + Logback** - Logging
- **Gradle** - Build tool

##  Instalasi

### Prasyarat
- Java 21 atau lebih tinggi
- PostgreSQL 12 atau lebih tinggi
- Gradle 8.x (atau gunakan Gradle Wrapper)

### Konfigurasi Database

1. Buat database di PostgreSQL:
```sql
CREATE DATABASE cronos_db;
```

2. Sesuaikan konfigurasi di `src/main/resources/hibernate.cfg.xml`:
```xml
<property name="hibernate.connection.url">jdbc:postgresql://localhost:5433/cronos_db</property>
<property name="hibernate.connection.username">postgres</property>
<property name="hibernate.connection.password">joko1453</property>
```

### Build & Run

```bash
# Build aplikasi
./gradlew build

# Jalankan aplikasi
./gradlew run --console=plain
```

##  Penggunaan

### Login Default
- **Username:** admin
- **Password:** admin123

### Menu Utama
1. **TRANSAKSI BARU** - Mulai transaksi penjualan
2. **Produk** - Manajemen produk dan stok
3. **Pelanggan** - Manajemen pelanggan dan member
4. **Laporan** - Lihat laporan dan dashboard
5. **Riwayat Transaksi** - Lihat dan cetak ulang transaksi
6. **Manajemen User** - Kelola user (Admin only)
7. **Pengaturan** - Konfigurasi sistem
8. **Ganti Password** - Ubah password
9. **Logout** - Keluar dari akun

### Alur Transaksi
1. Pilih menu **TRANSAKSI BARU**
2. Tambah item dengan memasukkan kode/barcode produk
3. Atur qty sesuai kebutuhan
4. (Opsional) Set pelanggan untuk mendapatkan diskon member
5. (Opsional) Tambah diskon tambahan
6. Pilih **Bayar** untuk memproses pembayaran
7. Pilih metode pembayaran
8. Masukkan jumlah uang
9. Struk akan dicetak otomatis

##  Entity Relationship

```
User (1) ─────────────────> (*) Transaksi
                                    │
Pelanggan (1) ─────────────────> (*) ┘
                                    │
                                    ├──> (*) DetailTransaksi ──> (1) Produk
                                    │
                                    └──> (*) Pembayaran

Produk (*) ──> (1) Kategori
       (*) ──> (1) Supplier
       (1) ──> (*) StokHistory

Kategori (1) ──> (*) Kategori (self-reference untuk hierarki)

Supplier (1) ──> (*) PembelianSupplier ──> (*) DetailPembelian
```

##  Konfigurasi

### AppConfig.java
```java
// PPN
public static final double TAX_RATE = 0.11; // 11%

// Threshold Stok
public static final int LOW_STOCK_THRESHOLD = 10;
public static final int CRITICAL_STOCK_THRESHOLD = 5;

// Diskon
public static final double MAX_DISCOUNT_PERCENTAGE = 50.0;
public static final double MEMBER_DISCOUNT = 5.0;

// Keamanan
public static final int MIN_PASSWORD_LENGTH = 6;
public static final int MAX_LOGIN_ATTEMPTS = 3;
```

##  Sample Data

Aplikasi akan otomatis membuat sample data saat pertama kali dijalankan:
- 4 Kategori (Makanan, Minuman, ATK, Elektronik)
- 8 Produk dengan berbagai kategori
- 3 Pelanggan (2 member, 1 non-member)

##  Role & Permission

| Fitur | Admin | Supervisor | Kasir |
|-------|-------|------------|-------|
| Transaksi | ✅ | ✅ | ✅ |
| Produk | ✅ | ✅ | ✅ |
| Pelanggan | ✅ | ✅ | ✅ |
| Laporan | ✅ | ✅ | ✅ |
| Void Transaksi | ✅ | ✅ | ❌ |
| Manajemen User | ✅ | ✅ | ❌ |
| Pengaturan | ✅ | ❌ | ❌ |

## 🐛 Troubleshooting

### Database Connection Error
- Pastikan PostgreSQL berjalan
- Cek port dan credentials di hibernate.cfg.xml
- Pastikan database sudah dibuat

### Class Not Found Error
- Jalankan `./gradlew clean build`

### Login Failed
- Default credentials: admin/admin123
- Jika user terkunci, gunakan admin lain untuk unlock

## 📄 Lisensi

MIT License

## 👨‍💻 Author

**Joko** - Aplikasi Java 2024

---
*KASIR PRO - Solusi POS Modern untuk Bisnis Anda*
