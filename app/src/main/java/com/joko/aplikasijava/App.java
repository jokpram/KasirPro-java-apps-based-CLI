package com.joko.aplikasijava;

import com.joko.aplikasijava.config.AppConfig;
import com.joko.aplikasijava.config.HibernateUtil;
import com.joko.aplikasijava.models.*;
import com.joko.aplikasijava.services.*;
import com.joko.aplikasijava.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aplikasi Sistem Kasir - KASIR PRO
 * Aplikasi Point of Sale berbasis CLI dengan ORM Hibernate dan PostgreSQL
 * 
 * @author Joko
 * @version 1.0.0
 */
public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    private final AuthService authService;
    private final ProdukService produkService;
    private final PelangganService pelangganService;
    private final TransaksiService transaksiService;
    private final LaporanService laporanService;
    
    private boolean running = true;
    
    public App() {
        this.authService = new AuthService();
        this.produkService = new ProdukService();
        this.pelangganService = new PelangganService();
        this.transaksiService = new TransaksiService();
        this.laporanService = new LaporanService();
    }
    
    public static void main(String[] args) {
        App app = new App();
        
        try {
            app.run();
        } catch (Exception e) {
            logger.error("Error aplikasi: {}", e.getMessage(), e);
            System.err.println("Terjadi kesalahan: " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
        }
    }
    
    /**
     * Menjalankan aplikasi
     */
    public void run() {
        showSplashScreen();
        
        // Inisialisasi admin default
        authService.initDefaultAdmin();
        initSampleData();
        
        // Login
        if (!doLogin()) {
            System.out.println("Gagal login. Aplikasi ditutup.");
            return;
        }
        
        // Main loop
        while (running) {
            showMainMenu();
            int choice = InputUtils.readMenu("Pilih menu: ", 9);
            handleMainMenu(choice);
        }
        
        System.out.println("\nTerima kasih telah menggunakan " + AppConfig.APP_NAME);
    }
    
    /**
     * Tampilkan splash screen
     */
    private void showSplashScreen() {
        InputUtils.clearScreen();
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║    ██╗  ██╗ █████╗ ███████╗██╗██████╗     ██████╗ ██████╗  ██████╗  ║");
        System.out.println("║    ██║ ██╔╝██╔══██╗██╔════╝██║██╔══██╗    ██╔══██╗██╔══██╗██╔═══██╗ ║");
        System.out.println("║    █████╔╝ ███████║███████╗██║██████╔╝    ██████╔╝██████╔╝██║   ██║ ║");
        System.out.println("║    ██╔═██╗ ██╔══██║╚════██║██║██╔══██╗    ██╔═══╝ ██╔══██╗██║   ██║ ║");
        System.out.println("║    ██║  ██╗██║  ██║███████║██║██║  ██║    ██║     ██║  ██║╚██████╔╝ ║");
        System.out.println("║    ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚═╝╚═╝  ╚═╝    ╚═╝     ╚═╝  ╚═╝ ╚═════╝  ║");
        System.out.println("║                                                          ║");
        System.out.println("║              SISTEM KASIR MODERN v" + AppConfig.APP_VERSION + "                ║");
        System.out.println("║                     by " + AppConfig.APP_AUTHOR + "                          ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Menginisialisasi sistem...");
        
        // Simulasi loading
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Proses login
     */
    private boolean doLogin() {
        System.out.println("\n========== LOGIN ==========");
        
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("\nPercobaan " + attempt + " dari 3");
            String username = InputUtils.readString("Username: ");
            String password = InputUtils.readString("Password: ");
            
            User user = authService.login(username, password);
            if (user != null) {
                System.out.println("\n✓ Selamat datang, " + user.getNamaLengkap() + "!");
                System.out.println("Role: " + user.getRole());
                InputUtils.pause();
                return true;
            } else {
                System.out.println("✗ Username atau password salah.");
            }
        }
        
        return false;
    }
    
    /**
     * Tampilkan menu utama
     */
    private void showMainMenu() {
        InputUtils.clearScreen();
        User user = authService.getCurrentUser();
        
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║              " + AppConfig.APP_NAME + " - MENU UTAMA              ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  User: %-20s Role: %-10s ║%n", 
                FormatUtils.truncate(user.getNamaLengkap(), 20), user.getRole());
        System.out.printf("║  Tanggal: %-38s ║%n", FormatUtils.formatDateTime(java.time.LocalDateTime.now()));
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║                                                  ║");
        System.out.println("║   [1] TRANSAKSI BARU                             ║");
        System.out.println("║   [2] Produk                                     ║");
        System.out.println("║   [3] Pelanggan                                  ║");
        System.out.println("║   [4] Laporan                                    ║");
        System.out.println("║   [5] Riwayat Transaksi                          ║");
        System.out.println("║                                                  ║");
        
        if (authService.isAdmin() || authService.isSupervisor()) {
            System.out.println("║   [6] Manajemen User                             ║");
            System.out.println("║   [7] Pengaturan                                 ║");
        }
        
        System.out.println("║                                                  ║");
        System.out.println("║   [8] Ganti Password                             ║");
        System.out.println("║   [9] Logout                                     ║");
        System.out.println("║   [0] Keluar                                     ║");
        System.out.println("║                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Handle pilihan menu utama
     */
    private void handleMainMenu(int choice) {
        switch (choice) {
            case 1 -> menuTransaksi();
            case 2 -> menuProduk();
            case 3 -> menuPelanggan();
            case 4 -> menuLaporan();
            case 5 -> menuRiwayatTransaksi();
            case 6 -> {
                if (authService.isAdmin() || authService.isSupervisor()) {
                    menuManajemenUser();
                } else {
                    System.out.println("Akses ditolak.");
                    InputUtils.pause();
                }
            }
            case 7 -> {
                if (authService.isAdmin()) {
                    menuPengaturan();
                } else {
                    System.out.println("Akses ditolak.");
                    InputUtils.pause();
                }
            }
            case 8 -> menuGantiPassword();
            case 9 -> {
                authService.logout();
                System.out.println("Logout berhasil.");
                if (!doLogin()) {
                    running = false;
                }
            }
            case 0 -> {
                if (InputUtils.readBoolean("Yakin ingin keluar?")) {
                    running = false;
                }
            }
            default -> {
                System.out.println("Pilihan tidak valid.");
                InputUtils.pause();
            }
        }
    }
    
    // ==================== MENU TRANSAKSI ====================
    
    private void menuTransaksi() {
        // Mulai transaksi baru
        Transaksi trx = transaksiService.mulaiTransaksiBaru(authService.getCurrentUser());
        
        boolean inTransaction = true;
        while (inTransaction) {
            showTransaksiScreen(trx);
            
            System.out.println("\n[1] Tambah Item  [2] Edit Qty  [3] Hapus Item");
            System.out.println("[4] Set Pelanggan  [5] Set Diskon  [6] Bayar");
            System.out.println("[7] Kosongkan  [0] Batalkan");
            
            int choice = InputUtils.readMenu("Pilih: ", 7);
            
            switch (choice) {
                case 1 -> tambahItemTransaksi();
                case 2 -> editQtyItem();
                case 3 -> hapusItemTransaksi();
                case 4 -> setPelangganTransaksi();
                case 5 -> setDiskonTransaksi();
                case 6 -> {
                    if (prosesPayment()) {
                        inTransaction = false;
                    }
                }
                case 7 -> {
                    if (InputUtils.readBoolean("Yakin ingin mengosongkan keranjang?")) {
                        transaksiService.kosongkanKeranjang();
                    }
                }
                case 0 -> {
                    if (InputUtils.readBoolean("Yakin ingin membatalkan transaksi?")) {
                        transaksiService.batalkanTransaksiAktif();
                        inTransaction = false;
                    }
                }
            }
        }
    }
    
    private void showTransaksiScreen(Transaksi trx) {
        InputUtils.clearScreen();
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("                      TRANSAKSI BARU");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("No. Transaksi: " + trx.getNomorTransaksi());
        if (trx.getPelanggan() != null) {
            System.out.println("Pelanggan    : " + trx.getPelanggan().getNama() + 
                    (trx.getPelanggan().getKodeMember() != null ? " [" + trx.getPelanggan().getKodeMember() + "]" : ""));
        }
        System.out.println("───────────────────────────────────────────────────────────");
        
        // Tampilkan item di keranjang
        List<DetailTransaksi> keranjang = transaksiService.getKeranjang();
        if (keranjang.isEmpty()) {
            System.out.println("\n           Keranjang kosong. Tambahkan item.\n");
        } else {
            System.out.printf("%-3s %-25s %8s %12s %15s%n", "No", "Nama Produk", "Qty", "Harga", "Subtotal");
            System.out.println("───────────────────────────────────────────────────────────");
            
            int no = 1;
            for (DetailTransaksi item : keranjang) {
                System.out.printf("%-3d %-25s %8d %12s %15s%n",
                        no++,
                        FormatUtils.truncate(item.getNamaProduk(), 25),
                        item.getQty(),
                        FormatUtils.formatRupiah(item.getHargaSatuan()),
                        FormatUtils.formatRupiah(item.getSubtotal()));
            }
        }
        
        System.out.println("───────────────────────────────────────────────────────────");
        System.out.printf("Subtotal: %50s%n", FormatUtils.formatRupiah(trx.getSubtotal()));
        
        if (trx.getDiskonPersen() != null && trx.getDiskonPersen().compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("Diskon (%s%%): %45s%n", trx.getDiskonPersen(), 
                    "-" + FormatUtils.formatRupiah(trx.getSubtotal().multiply(trx.getDiskonPersen()).divide(new BigDecimal("100"))));
        }
        if (trx.getPpnNominal() != null && trx.getPpnNominal().compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("PPN (%s%%): %47s%n", trx.getPpnPersen(), FormatUtils.formatRupiah(trx.getPpnNominal()));
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.printf("GRAND TOTAL: %47s%n", FormatUtils.formatRupiah(trx.getGrandTotal()));
        System.out.println("═══════════════════════════════════════════════════════════");
    }
    
    private void tambahItemTransaksi() {
        String kode = InputUtils.readString("Kode/Barcode produk (kosong untuk batal): ");
        if (kode.isEmpty()) return;
        
        try {
            int qty = InputUtils.readInt("Qty: ", 1, 9999);
            transaksiService.tambahKeKeranjang(kode, qty);
            System.out.println("✓ Item berhasil ditambahkan");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            InputUtils.pause();
        }
    }
    
    private void editQtyItem() {
        List<DetailTransaksi> keranjang = transaksiService.getKeranjang();
        if (keranjang.isEmpty()) {
            System.out.println("Keranjang kosong.");
            InputUtils.pause();
            return;
        }
        
        int index = InputUtils.readInt("No item yang akan diedit: ", 1, keranjang.size()) - 1;
        int newQty = InputUtils.readInt("Qty baru (0 untuk hapus): ", 0, 9999);
        
        try {
            transaksiService.updateQtyKeranjang(index, newQty);
            System.out.println("✓ Qty berhasil diupdate");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            InputUtils.pause();
        }
    }
    
    private void hapusItemTransaksi() {
        List<DetailTransaksi> keranjang = transaksiService.getKeranjang();
        if (keranjang.isEmpty()) {
            System.out.println("Keranjang kosong.");
            InputUtils.pause();
            return;
        }
        
        int index = InputUtils.readInt("No item yang akan dihapus: ", 1, keranjang.size()) - 1;
        transaksiService.hapusDariKeranjang(index);
        System.out.println("✓ Item berhasil dihapus");
    }
    
    private void setPelangganTransaksi() {
        String input = InputUtils.readString("Kode member/No telepon (kosong untuk batal): ");
        if (input.isEmpty()) return;
        
        try {
            transaksiService.setPelanggan(input);
            System.out.println("✓ Pelanggan berhasil diset");
        } catch (Exception e) {
            System.out.println("✗ Pelanggan tidak ditemukan");
            InputUtils.pause();
        }
    }
    
    private void setDiskonTransaksi() {
        BigDecimal diskonPersen = InputUtils.readBigDecimal("Diskon persen (0 untuk skip): ", BigDecimal.ZERO);
        BigDecimal diskonNominal = InputUtils.readBigDecimal("Diskon nominal (0 untuk skip): ", BigDecimal.ZERO);
        
        try {
            transaksiService.setDiskon(diskonPersen, diskonNominal);
            System.out.println("✓ Diskon berhasil diset");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            InputUtils.pause();
        }
    }
    
    private boolean prosesPayment() {
        if (transaksiService.getKeranjang().isEmpty()) {
            System.out.println("Keranjang kosong. Tidak dapat memproses pembayaran.");
            InputUtils.pause();
            return false;
        }
        
        Transaksi trx = transaksiService.getTransaksiAktif();
        
        System.out.println("\n══════════ PEMBAYARAN ══════════");
        System.out.println("Total yang harus dibayar: " + FormatUtils.formatRupiah(trx.getGrandTotal()));
        System.out.println();
        System.out.println("[1] TUNAI  [2] DEBIT  [3] KREDIT  [4] QRIS  [5] TRANSFER");
        
        int metodeChoice = InputUtils.readMenu("Metode pembayaran: ", 5);
        String metodePembayaran = switch (metodeChoice) {
            case 1 -> "TUNAI";
            case 2 -> "DEBIT";
            case 3 -> "KREDIT";
            case 4 -> "QRIS";
            case 5 -> "TRANSFER";
            default -> "TUNAI";
        };
        
        BigDecimal jumlahBayar = InputUtils.readBigDecimal("Jumlah bayar: ");
        
        if (jumlahBayar.compareTo(trx.getGrandTotal()) < 0) {
            System.out.println("✗ Jumlah bayar kurang!");
            InputUtils.pause();
            return false;
        }
        
        String noRef = null;
        if (metodeChoice >= 2) {
            noRef = InputUtils.readString("No. Referensi (optional): ");
        }
        
        try {
            Transaksi completed = transaksiService.prosesPembayaran(metodePembayaran, jumlahBayar, noRef);
            
            // Cetak struk
            System.out.println(StrukUtils.cetakStruk(completed));
            
            InputUtils.pause("Tekan ENTER untuk kembali ke menu...");
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            InputUtils.pause();
            return false;
        }
    }
    
    // ==================== MENU PRODUK ====================
    
    private void menuProduk() {
        boolean back = false;
        while (!back) {
            InputUtils.clearScreen();
            System.out.println("\n========== MANAJEMEN PRODUK ==========");
            System.out.println("[1] Lihat Semua Produk");
            System.out.println("[2] Cari Produk");
            System.out.println("[3] Tambah Produk Baru");
            System.out.println("[4] Edit Produk");
            System.out.println("[5] Tambah Stok");
            System.out.println("[6] Produk Stok Rendah");
            System.out.println("[7] Kelola Kategori");
            System.out.println("[0] Kembali");
            
            int choice = InputUtils.readMenu("Pilih: ", 7);
            
            switch (choice) {
                case 1 -> lihatSemuaProduk();
                case 2 -> cariProduk();
                case 3 -> tambahProdukBaru();
                case 4 -> editProduk();
                case 5 -> tambahStokProduk();
                case 6 -> lihatProdukStokRendah();
                case 7 -> kelolaKategori();
                case 0 -> back = true;
            }
        }
    }
    
    private void lihatSemuaProduk() {
        List<Produk> produkList = produkService.getAllProdukAktif();
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════════");
        System.out.println("                           DAFTAR PRODUK");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("%-8s %-30s %12s %8s %-10s%n", "Kode", "Nama", "Harga Jual", "Stok", "Satuan");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        
        for (Produk p : produkList) {
            String stokStatus = p.isLowStock() ? (p.isOutOfStock() ? "!!" : "!") : "";
            System.out.printf("%-8s %-30s %12s %7d%s %-10s%n",
                    p.getKode(),
                    FormatUtils.truncate(p.getNama(), 30),
                    FormatUtils.formatRupiah(p.getHargaJual()),
                    p.getStok(),
                    stokStatus,
                    p.getSatuan());
        }
        
        System.out.println("───────────────────────────────────────────────────────────────────────");
        System.out.println("Total: " + produkList.size() + " produk. (! = stok rendah, !! = habis)");
        InputUtils.pause();
    }
    
    private void cariProduk() {
        String keyword = InputUtils.readString("Cari (kode/nama/barcode): ");
        List<Produk> hasil = produkService.cariProduk(keyword);
        
        if (hasil.isEmpty()) {
            System.out.println("Produk tidak ditemukan.");
        } else {
            System.out.printf("%nDitemukan %d produk:%n", hasil.size());
            for (Produk p : hasil) {
                System.out.printf("- [%s] %s | %s | Stok: %d%n", 
                        p.getKode(), p.getNama(), FormatUtils.formatRupiah(p.getHargaJual()), p.getStok());
            }
        }
        InputUtils.pause();
    }
    
    private void tambahProdukBaru() {
        System.out.println("\n========== TAMBAH PRODUK BARU ==========");
        
        String kode = InputUtils.readString("Kode produk: ");
        String barcode = InputUtils.readString("Barcode (optional): ");
        String nama = InputUtils.readString("Nama produk: ");
        BigDecimal hargaBeli = InputUtils.readBigDecimal("Harga beli: ");
        BigDecimal hargaJual = InputUtils.readBigDecimal("Harga jual: ");
        int stok = InputUtils.readInt("Stok awal: ", 0, 999999);
        String satuan = InputUtils.readString("Satuan (PCS/KG/BOX/etc): ", "PCS");
        
        try {
            Produk produk = new Produk(kode, nama, hargaBeli, hargaJual, stok, satuan);
            if (!barcode.isEmpty()) {
                produk.setBarcode(barcode);
            }
            
            produkService.tambahProduk(produk);
            System.out.println("✓ Produk berhasil ditambahkan!");
        } catch (Exception e) {
            System.out.println("✗ Gagal menambah produk: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    private void editProduk() {
        String kode = InputUtils.readString("Masukkan kode produk yang akan diedit: ");
        var produkOpt = produkService.getProdukByKodeOrBarcode(kode);
        
        if (produkOpt.isEmpty()) {
            System.out.println("Produk tidak ditemukan.");
            InputUtils.pause();
            return;
        }
        
        Produk produk = produkOpt.get();
        System.out.println("\nData produk saat ini:");
        System.out.println("Kode: " + produk.getKode());
        System.out.println("Nama: " + produk.getNama());
        System.out.println("Harga Beli: " + FormatUtils.formatRupiah(produk.getHargaBeli()));
        System.out.println("Harga Jual: " + FormatUtils.formatRupiah(produk.getHargaJual()));
        System.out.println("Stok: " + produk.getStok());
        
        System.out.println("\nKosongkan untuk tidak mengubah:");
        
        String newNama = InputUtils.readString("Nama baru: ");
        if (!newNama.isEmpty()) produk.setNama(newNama);
        
        String hargaBeliStr = InputUtils.readString("Harga beli baru: ");
        if (!hargaBeliStr.isEmpty()) produk.setHargaBeli(new BigDecimal(hargaBeliStr));
        
        String hargaJualStr = InputUtils.readString("Harga jual baru: ");
        if (!hargaJualStr.isEmpty()) produk.setHargaJual(new BigDecimal(hargaJualStr));
        
        try {
            produkService.updateProduk(produk);
            System.out.println("✓ Produk berhasil diupdate!");
        } catch (Exception e) {
            System.out.println("✗ Gagal mengupdate: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    private void tambahStokProduk() {
        String kode = InputUtils.readString("Kode produk: ");
        var produkOpt = produkService.getProdukByKodeOrBarcode(kode);
        
        if (produkOpt.isEmpty()) {
            System.out.println("Produk tidak ditemukan.");
            InputUtils.pause();
            return;
        }
        
        Produk produk = produkOpt.get();
        System.out.printf("Produk: %s | Stok saat ini: %d%n", produk.getNama(), produk.getStok());
        
        int qty = InputUtils.readInt("Jumlah stok yang ditambahkan: ", 1, 999999);
        String keterangan = InputUtils.readString("Keterangan: ", "Penambahan stok");
        
        try {
            produkService.tambahStok(produk.getId(), qty, keterangan, authService.getCurrentUser());
            System.out.println("✓ Stok berhasil ditambahkan!");
        } catch (Exception e) {
            System.out.println("✗ Gagal menambah stok: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    private void lihatProdukStokRendah() {
        List<Produk> produkList = produkService.getProdukStokRendah();
        
        System.out.println("\n========== PRODUK STOK RENDAH ==========");
        if (produkList.isEmpty()) {
            System.out.println("Tidak ada produk dengan stok rendah.");
        } else {
            for (Produk p : produkList) {
                System.out.printf("[%s] %s | Stok: %d / Min: %d%n", 
                        p.getKode(), p.getNama(), p.getStok(), p.getStokMinimum());
            }
        }
        InputUtils.pause();
    }
    
    private void kelolaKategori() {
        System.out.println("\n========== DAFTAR KATEGORI ==========");
        List<Kategori> kategoriList = produkService.getAllKategoriAktif();
        
        for (Kategori k : kategoriList) {
            System.out.printf("[%s] %s%n", k.getKode(), k.getNama());
        }
        
        System.out.println("\n[1] Tambah Kategori  [0] Kembali");
        int choice = InputUtils.readMenu("Pilih: ", 1);
        
        if (choice == 1) {
            String kode = InputUtils.readString("Kode kategori: ");
            String nama = InputUtils.readString("Nama kategori: ");
            
            try {
                Kategori kategori = new Kategori(kode, nama);
                produkService.tambahKategori(kategori);
                System.out.println("✓ Kategori berhasil ditambahkan!");
            } catch (Exception e) {
                System.out.println("✗ Gagal: " + e.getMessage());
            }
            InputUtils.pause();
        }
    }
    
    // ==================== MENU PELANGGAN ====================
    
    private void menuPelanggan() {
        boolean back = false;
        while (!back) {
            InputUtils.clearScreen();
            System.out.println("\n========== MANAJEMEN PELANGGAN ==========");
            System.out.println("[1] Lihat Semua Pelanggan");
            System.out.println("[2] Cari Pelanggan");
            System.out.println("[3] Tambah Member Baru");
            System.out.println("[4] Tambah Pelanggan Biasa");
            System.out.println("[5] Top Member");
            System.out.println("[0] Kembali");
            
            int choice = InputUtils.readMenu("Pilih: ", 5);
            
            switch (choice) {
                case 1 -> lihatSemuaPelanggan();
                case 2 -> cariPelanggan();
                case 3 -> tambahMemberBaru();
                case 4 -> tambahPelangganBiasa();
                case 5 -> lihatTopMember();
                case 0 -> back = true;
            }
        }
    }
    
    private void lihatSemuaPelanggan() {
        List<Pelanggan> pelangganList = pelangganService.getAllPelangganAktif();
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════════");
        System.out.printf("%-10s %-25s %-15s %-10s %10s%n", "Kode", "Nama", "No Telepon", "Tipe", "Poin");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        
        for (Pelanggan p : pelangganList) {
            System.out.printf("%-10s %-25s %-15s %-10s %10d%n",
                    p.getKodeMember() != null ? p.getKodeMember() : "-",
                    FormatUtils.truncate(p.getNama(), 25),
                    p.getNoTelepon() != null ? p.getNoTelepon() : "-",
                    p.getTipeMember(),
                    p.getPoin());
        }
        
        System.out.println("───────────────────────────────────────────────────────────────────────");
        System.out.println("Total: " + pelangganList.size() + " pelanggan");
        InputUtils.pause();
    }
    
    private void cariPelanggan() {
        String keyword = InputUtils.readString("Cari (nama/kode member/telepon): ");
        List<Pelanggan> hasil = pelangganService.cariPelanggan(keyword);
        
        if (hasil.isEmpty()) {
            System.out.println("Pelanggan tidak ditemukan.");
        } else {
            System.out.printf("%nDitemukan %d pelanggan:%n", hasil.size());
            for (Pelanggan p : hasil) {
                System.out.printf("- [%s] %s | %s | Poin: %d%n", 
                        p.getKodeMember() != null ? p.getKodeMember() : "-",
                        p.getNama(), 
                        p.getNoTelepon() != null ? p.getNoTelepon() : "-",
                        p.getPoin());
            }
        }
        InputUtils.pause();
    }
    
    private void tambahMemberBaru() {
        System.out.println("\n========== TAMBAH MEMBER BARU ==========");
        
        String nama = InputUtils.readString("Nama: ");
        String noTelepon = InputUtils.readString("No Telepon: ");
        String email = InputUtils.readString("Email (optional): ");
        String alamat = InputUtils.readString("Alamat (optional): ");
        
        try {
            Pelanggan member = pelangganService.tambahMember(nama, noTelepon, 
                    email.isEmpty() ? null : email, 
                    alamat.isEmpty() ? null : alamat);
            System.out.println("✓ Member berhasil ditambahkan!");
            System.out.println("  Kode Member: " + member.getKodeMember());
        } catch (Exception e) {
            System.out.println("✗ Gagal: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    private void tambahPelangganBiasa() {
        System.out.println("\n========== TAMBAH PELANGGAN ==========");
        
        String nama = InputUtils.readString("Nama: ");
        String noTelepon = InputUtils.readString("No Telepon (optional): ");
        
        try {
            Pelanggan pelanggan = new Pelanggan(nama);
            if (!noTelepon.isEmpty()) pelanggan.setNoTelepon(noTelepon);
            pelangganService.tambahPelanggan(pelanggan);
            System.out.println("✓ Pelanggan berhasil ditambahkan!");
        } catch (Exception e) {
            System.out.println("✗ Gagal: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    private void lihatTopMember() {
        System.out.println("\n========== TOP MEMBER ==========");
        
        System.out.println("\nBerdasarkan Total Belanja:");
        List<Pelanggan> topBelanja = pelangganService.getTopPelangganByBelanja(10);
        int no = 1;
        for (Pelanggan p : topBelanja) {
            System.out.printf("%2d. %s | %s%n", no++, p.getNama(), FormatUtils.formatRupiah(p.getTotalBelanja()));
        }
        
        System.out.println("\nBerdasarkan Poin:");
        List<Pelanggan> topPoin = pelangganService.getTopPelangganByPoin(10);
        no = 1;
        for (Pelanggan p : topPoin) {
            System.out.printf("%2d. %s | %d poin%n", no++, p.getNama(), p.getPoin());
        }
        
        InputUtils.pause();
    }
    
    // ==================== MENU LAPORAN ====================
    
    private void menuLaporan() {
        boolean back = false;
        while (!back) {
            InputUtils.clearScreen();
            System.out.println("\n========== LAPORAN ==========");
            System.out.println("[1] Dashboard Ringkasan");
            System.out.println("[2] Laporan Penjualan Hari Ini");
            System.out.println("[3] Laporan Penjualan Per Tanggal");
            System.out.println("[4] Produk Terlaris");
            System.out.println("[5] Laporan Stok");
            System.out.println("[0] Kembali");
            
            int choice = InputUtils.readMenu("Pilih: ", 5);
            
            switch (choice) {
                case 1 -> showDashboard();
                case 2 -> laporanPenjualanHariIni();
                case 3 -> laporanPenjualanPerTanggal();
                case 4 -> laporanProdukTerlaris();
                case 5 -> laporanStok();
                case 0 -> back = true;
            }
        }
    }
    
    private void showDashboard() {
        LaporanService.DashboardData data = laporanService.getDashboardData();
        
        InputUtils.clearScreen();
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                      DASHBOARD                           ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf("║  Tanggal: %-46s ║%n", FormatUtils.formatTanggal(LocalDate.now()));
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.printf("║  💰 Penjualan Hari Ini: %-32s ║%n", FormatUtils.formatRupiah(data.getPenjualanHariIni()));
        System.out.printf("║  📊 Transaksi Hari Ini: %-32s ║%n", data.getTransaksiHariIni() + " transaksi");
        System.out.println("║                                                          ║");
        System.out.printf("║  📦 Total Produk Aktif: %-32s ║%n", data.getTotalProduk());
        System.out.printf("║  ⚠️  Produk Stok Rendah: %-31s ║%n", data.getProdukStokRendah());
        System.out.printf("║  💎 Nilai Stok: %-40s ║%n", FormatUtils.formatRupiah(data.getNilaiStok()));
        System.out.println("║                                                          ║");
        System.out.printf("║  👥 Total Member: %-38s ║%n", data.getTotalMember());
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        
        InputUtils.pause();
    }
    
    private void laporanPenjualanHariIni() {
        LaporanService.RingkasanPenjualan ringkasan = laporanService.getRingkasanHariIni();
        tampilkanRingkasanPenjualan(ringkasan);
    }
    
    private void laporanPenjualanPerTanggal() {
        LocalDate tanggal = InputUtils.readDate("Masukkan tanggal", LocalDate.now());
        LaporanService.RingkasanPenjualan ringkasan = laporanService.getRingkasanByTanggal(tanggal);
        tampilkanRingkasanPenjualan(ringkasan);
    }
    
    private void tampilkanRingkasanPenjualan(LaporanService.RingkasanPenjualan r) {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("                  RINGKASAN PENJUALAN");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.printf("Periode: %s - %s%n", FormatUtils.formatTanggal(r.getTanggalMulai()), 
                FormatUtils.formatTanggal(r.getTanggalSelesai()));
        System.out.println("───────────────────────────────────────────────────────────");
        System.out.printf("Jumlah Transaksi    : %d%n", r.getJumlahTransaksi());
        System.out.printf("Jumlah Item Terjual : %d%n", r.getJumlahItem());
        System.out.printf("Total Penjualan     : %s%n", FormatUtils.formatRupiah(r.getTotalPenjualan()));
        System.out.printf("Total Diskon        : %s%n", FormatUtils.formatRupiah(r.getTotalDiskon()));
        System.out.printf("Total PPN           : %s%n", FormatUtils.formatRupiah(r.getTotalPPN()));
        System.out.println("───────────────────────────────────────────────────────────");
        System.out.printf("Pembayaran Tunai    : %s%n", FormatUtils.formatRupiah(r.getTunai()));
        System.out.printf("Pembayaran Non-Tunai: %s%n", FormatUtils.formatRupiah(r.getNonTunai()));
        System.out.println("───────────────────────────────────────────────────────────");
        System.out.printf("Transaksi Member    : %d%n", r.getTransaksiMember());
        System.out.printf("Transaksi Umum      : %d%n", r.getTransaksiUmum());
        System.out.printf("Rata-rata/Transaksi : %s%n", FormatUtils.formatRupiah(r.getRataRataTransaksi()));
        System.out.println("═══════════════════════════════════════════════════════════");
        
        InputUtils.pause();
    }
    
    private void laporanProdukTerlaris() {
        int limit = InputUtils.readInt("Tampilkan berapa produk? ", 1, 50);
        List<Produk> produkList = laporanService.getProdukTerlaris(limit);
        
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("                    PRODUK TERLARIS");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.printf("%-4s %-30s %15s%n", "No", "Nama Produk", "Terjual");
        System.out.println("───────────────────────────────────────────────────────────");
        
        int no = 1;
        for (Produk p : produkList) {
            System.out.printf("%-4d %-30s %15d%n", no++, FormatUtils.truncate(p.getNama(), 30), p.getTerjual());
        }
        
        InputUtils.pause();
    }
    
    private void laporanStok() {
        BigDecimal totalNilaiStok = laporanService.getTotalNilaiStok();
        long jumlahProduk = laporanService.getJumlahProduk();
        List<Produk> stokRendah = laporanService.getProdukStokRendah();
        List<Produk> stokHabis = laporanService.getProdukHabisStok();
        
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("                    LAPORAN STOK");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.printf("Total Produk Aktif   : %d%n", jumlahProduk);
        System.out.printf("Total Nilai Stok     : %s%n", FormatUtils.formatRupiah(totalNilaiStok));
        System.out.printf("Produk Stok Rendah   : %d%n", stokRendah.size());
        System.out.printf("Produk Stok Habis    : %d%n", stokHabis.size());
        
        if (!stokHabis.isEmpty()) {
            System.out.println("\nProduk HABIS STOK:");
            for (Produk p : stokHabis) {
                System.out.printf("  - [%s] %s%n", p.getKode(), p.getNama());
            }
        }
        
        InputUtils.pause();
    }
    
    // ==================== MENU RIWAYAT TRANSAKSI ====================
    
    private void menuRiwayatTransaksi() {
        List<Transaksi> transaksiList = transaksiService.getTransaksiHariIni();
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════════");
        System.out.println("                    RIWAYAT TRANSAKSI HARI INI");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("%-18s %-12s %-20s %15s %-10s%n", "No. Transaksi", "Waktu", "Kasir", "Total", "Status");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        
        for (Transaksi t : transaksiList) {
            System.out.printf("%-18s %-12s %-20s %15s %-10s%n",
                    t.getNomorTransaksi(),
                    FormatUtils.formatWaktu(t.getTanggalTransaksi()),
                    FormatUtils.truncate(t.getKasir().getNamaLengkap(), 20),
                    FormatUtils.formatRupiah(t.getGrandTotal()),
                    t.getStatus());
        }
        
        System.out.println("───────────────────────────────────────────────────────────────────────");
        System.out.println("Total: " + transaksiList.size() + " transaksi");
        
        System.out.println("\n[1] Lihat Detail Transaksi  [2] Cetak Ulang Struk  [0] Kembali");
        int choice = InputUtils.readMenu("Pilih: ", 2);
        
        if (choice == 1 || choice == 2) {
            String nomorTrx = InputUtils.readString("No. Transaksi: ");
            var trxOpt = transaksiService.getTransaksiByNomor(nomorTrx);
            if (trxOpt.isPresent()) {
                if (choice == 2) {
                    System.out.println(StrukUtils.cetakStruk(trxOpt.get()));
                } else {
                    Transaksi t = trxOpt.get();
                    System.out.println("\nDetail Transaksi: " + t.getNomorTransaksi());
                    System.out.println("Tanggal: " + FormatUtils.formatDateTime(t.getTanggalTransaksi()));
                    System.out.println("Kasir: " + t.getKasir().getNamaLengkap());
                    System.out.println("Status: " + t.getStatus());
                    System.out.println("Total: " + FormatUtils.formatRupiah(t.getGrandTotal()));
                }
            } else {
                System.out.println("Transaksi tidak ditemukan.");
            }
        }
        
        InputUtils.pause();
    }
    
    // ==================== MENU MANAJEMEN USER ====================
    
    private void menuManajemenUser() {
        boolean back = false;
        while (!back) {
            InputUtils.clearScreen();
            System.out.println("\n========== MANAJEMEN USER ==========");
            System.out.println("[1] Lihat Semua User");
            System.out.println("[2] Tambah User Baru");
            System.out.println("[3] Reset Password User");
            System.out.println("[4] Unlock User");
            System.out.println("[0] Kembali");
            
            int choice = InputUtils.readMenu("Pilih: ", 4);
            
            switch (choice) {
                case 1 -> lihatSemuaUser();
                case 2 -> tambahUserBaru();
                case 3 -> resetPasswordUser();
                case 4 -> unlockUser();
                case 0 -> back = true;
            }
        }
    }
    
    private void lihatSemuaUser() {
        List<User> userList = authService.getAllUsers();
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════════");
        System.out.printf("%-15s %-25s %-12s %-8s %-10s%n", "Username", "Nama Lengkap", "Role", "Aktif", "Terkunci");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        
        for (User u : userList) {
            System.out.printf("%-15s %-25s %-12s %-8s %-10s%n",
                    u.getUsername(),
                    FormatUtils.truncate(u.getNamaLengkap(), 25),
                    u.getRole(),
                    u.getAktif() ? "Ya" : "Tidak",
                    u.getTerkunci() ? "Ya" : "Tidak");
        }
        
        InputUtils.pause();
    }
    
    private void tambahUserBaru() {
        System.out.println("\n========== TAMBAH USER BARU ==========");
        
        String username = InputUtils.readString("Username: ");
        String password = InputUtils.readString("Password: ");
        String namaLengkap = InputUtils.readString("Nama Lengkap: ");
        
        System.out.println("Role: [1] ADMIN  [2] SUPERVISOR  [3] KASIR");
        int roleChoice = InputUtils.readMenu("Pilih role: ", 3);
        String role = switch (roleChoice) {
            case 1 -> AppConfig.ROLE_ADMIN;
            case 2 -> AppConfig.ROLE_SUPERVISOR;
            default -> AppConfig.ROLE_KASIR;
        };
        
        try {
            authService.register(username, password, namaLengkap, role);
            System.out.println("✓ User berhasil ditambahkan!");
        } catch (Exception e) {
            System.out.println("✗ Gagal: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    private void resetPasswordUser() {
        String username = InputUtils.readString("Username yang akan direset: ");
        var userList = authService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .toList();
        
        if (userList.isEmpty()) {
            System.out.println("User tidak ditemukan.");
            InputUtils.pause();
            return;
        }
        
        String newPassword = InputUtils.readString("Password baru: ");
        try {
            authService.resetPassword(userList.get(0).getId(), newPassword);
            System.out.println("✓ Password berhasil direset!");
        } catch (Exception e) {
            System.out.println("✗ Gagal: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    private void unlockUser() {
        String username = InputUtils.readString("Username yang akan di-unlock: ");
        var userList = authService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .toList();
        
        if (userList.isEmpty()) {
            System.out.println("User tidak ditemukan.");
        } else {
            try {
                authService.unlockUser(userList.get(0).getId());
                System.out.println("✓ User berhasil di-unlock!");
            } catch (Exception e) {
                System.out.println("✗ Gagal: " + e.getMessage());
            }
        }
        InputUtils.pause();
    }
    
    // ==================== MENU PENGATURAN ====================
    
    private void menuPengaturan() {
        System.out.println("\n========== PENGATURAN ==========");
        System.out.println("PPN saat ini: " + (AppConfig.TAX_RATE * 100) + "%");
        System.out.println("Nama Toko: " + AppConfig.APP_NAME);
        System.out.println("\n(Pengaturan dapat diubah di AppConfig.java)");
        InputUtils.pause();
    }
    
    // ==================== GANTI PASSWORD ====================
    
    private void menuGantiPassword() {
        System.out.println("\n========== GANTI PASSWORD ==========");
        
        String oldPassword = InputUtils.readString("Password lama: ");
        String newPassword = InputUtils.readString("Password baru: ");
        String confirmPassword = InputUtils.readString("Konfirmasi password baru: ");
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("✗ Konfirmasi password tidak cocok!");
            InputUtils.pause();
            return;
        }
        
        try {
            if (authService.changePassword(authService.getCurrentUser().getId(), oldPassword, newPassword)) {
                System.out.println("✓ Password berhasil diubah!");
            } else {
                System.out.println("✗ Password lama salah!");
            }
        } catch (Exception e) {
            System.out.println("✗ Gagal: " + e.getMessage());
        }
        InputUtils.pause();
    }
    
    // ==================== SAMPLE DATA ====================
    
    /**
     * Inisialisasi sample data untuk demo
     */
    private void initSampleData() {
        try {
            // Cek apakah sudah ada data
            if (produkService.countProdukAktif() > 0) {
                return; // Data sudah ada
            }
            
            logger.info("Menginisialisasi sample data...");
            
            // Kategori
            Kategori makanan = new Kategori("KAT001", "Makanan");
            Kategori minuman = new Kategori("KAT002", "Minuman");
            Kategori atk = new Kategori("KAT003", "Alat Tulis");
            Kategori elektronik = new Kategori("KAT004", "Elektronik");
            
            produkService.tambahKategori(makanan);
            produkService.tambahKategori(minuman);
            produkService.tambahKategori(atk);
            produkService.tambahKategori(elektronik);
            
            // Produk Makanan
            Produk p1 = new Produk("MKN001", "Indomie Goreng", new BigDecimal("2500"), new BigDecimal("3500"), 100, "PCS");
            p1.setBarcode("8991002101234");
            p1.setKategori(makanan);
            produkService.tambahProduk(p1);
            
            Produk p2 = new Produk("MKN002", "Roti Tawar", new BigDecimal("10000"), new BigDecimal("14000"), 50, "PCS");
            p2.setKategori(makanan);
            produkService.tambahProduk(p2);
            
            Produk p3 = new Produk("MKN003", "Oreo Original", new BigDecimal("8000"), new BigDecimal("12000"), 75, "PCS");
            p3.setKategori(makanan);
            produkService.tambahProduk(p3);
            
            // Produk Minuman
            Produk p4 = new Produk("MNM001", "Aqua 600ml", new BigDecimal("2000"), new BigDecimal("3000"), 200, "BTL");
            p4.setBarcode("8991002201234");
            p4.setKategori(minuman);
            produkService.tambahProduk(p4);
            
            Produk p5 = new Produk("MNM002", "Coca Cola 390ml", new BigDecimal("5000"), new BigDecimal("7000"), 80, "BTL");
            p5.setKategori(minuman);
            produkService.tambahProduk(p5);
            
            Produk p6 = new Produk("MNM003", "Teh Botol Sosro", new BigDecimal("3000"), new BigDecimal("4500"), 120, "BTL");
            p6.setKategori(minuman);
            produkService.tambahProduk(p6);
            
            // Produk ATK
            Produk p7 = new Produk("ATK001", "Pulpen Pilot", new BigDecimal("3000"), new BigDecimal("5000"), 150, "PCS");
            p7.setKategori(atk);
            produkService.tambahProduk(p7);
            
            Produk p8 = new Produk("ATK002", "Buku Tulis 40 Lembar", new BigDecimal("4000"), new BigDecimal("6000"), 100, "PCS");
            p8.setKategori(atk);
            produkService.tambahProduk(p8);
            
            // Pelanggan
            pelangganService.tambahMember("Budi Santoso", "081234567890", "budi@email.com", "Jl. Merdeka No. 1");
            pelangganService.tambahMember("Siti Rahayu", "082345678901", "siti@email.com", "Jl. Sudirman No. 5");
            pelangganService.tambahPelanggan(new Pelanggan("Andi"));
            
            logger.info("Sample data berhasil diinisialisasi");
            
        } catch (Exception e) {
            logger.warn("Gagal menginisialisasi sample data: {}", e.getMessage());
        }
    }
}
