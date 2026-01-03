# KASIR PRO - Panduan Instalasi

## Persyaratan Sistem

### Minimum Requirements
- **Java**: JDK/JRE 21 atau lebih tinggi
- **Database**: PostgreSQL 12 atau lebih tinggi
- **RAM**: 512 MB (1 GB recommended)
- **Disk**: 100 MB free space
- **OS**: Windows 10/11, Linux, macOS

## Download Java

Jika belum memiliki Java, download dari:
- **Windows/Mac/Linux**: https://adoptium.net/temurin/releases/
- Pilih versi **JDK 21 (LTS)**

## Instalasi

### Windows
1. Pastikan Java 21+ terinstall
2. Extract folder `kasirpro` ke lokasi yang diinginkan (contoh: `C:\Program Files\KasirPro\`)
3. Double-click `KasirPro.bat` untuk menjalankan

### Linux/macOS
1. Pastikan Java 21+ terinstall
2. Extract folder ke lokasi yang diinginkan
3. Buka terminal dan jalankan:
   ```bash
   chmod +x kasirpro.sh
   ./kasirpro.sh
   ```

## Setup Database

### 1. Install PostgreSQL
Download dari: https://www.postgresql.org/download/

### 2. Buat Database
```sql
CREATE DATABASE cronos_db;
```

### 3. Konfigurasi Koneksi
Edit file `hibernate.cfg.xml` di dalam JAR jika perlu mengubah konfigurasi:
- Host: localhost (default)
- Port: 5433 (default)
- Database: cronos_db
- Username: postgres
- Password: joko1453

## Menjalankan Aplikasi

### Windows
```cmd
KasirPro.bat
```
atau
```cmd
java -jar kasirpro-1.0.0-all.jar
```

### Linux/macOS
```bash
./kasirpro.sh
```
atau
```bash
java -jar kasirpro-1.0.0-all.jar
```

## Login Default

| Username | Password | Role |
|----------|----------|------|
| admin    | admin123 | Administrator |

⚠️ **PENTING**: Segera ganti password default setelah login pertama!

## Troubleshooting

### "Java tidak ditemukan"
- Pastikan Java 21+ terinstall
- Pastikan JAVA_HOME sudah diset dengan benar
- Tambahkan Java ke PATH system

### "Connection refused" ke database
- Pastikan PostgreSQL berjalan
- Periksa port (default: 5433)
- Pastikan database `cronos_db` sudah dibuat

### Login gagal
- Gunakan kredensial default: admin/admin123
- Jika terkunci, hubungi administrator

## Kontak Support

Untuk bantuan lebih lanjut, hubungi:
- Email: support@kasirpro.com
- Website: https://kasirpro.com

---
© 2024 KASIR PRO - Sistem Kasir Modern
