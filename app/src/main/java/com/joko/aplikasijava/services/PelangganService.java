package com.joko.aplikasijava.services;

import com.joko.aplikasijava.models.Pelanggan;
import com.joko.aplikasijava.repositories.PelangganRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk manajemen pelanggan dan membership
 */
public class PelangganService {
    
    private static final Logger logger = LoggerFactory.getLogger(PelangganService.class);
    private final PelangganRepository pelangganRepository;
    
    public PelangganService() {
        this.pelangganRepository = new PelangganRepository();
    }
    
    /**
     * Tambah pelanggan baru
     */
    public Pelanggan tambahPelanggan(Pelanggan pelanggan) {
        // Validasi kode member jika ada
        if (pelanggan.getKodeMember() != null) {
            Optional<Pelanggan> existing = pelangganRepository.findByKodeMember(pelanggan.getKodeMember());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Kode member sudah digunakan");
            }
        }
        
        return pelangganRepository.save(pelanggan);
    }
    
    /**
     * Tambah pelanggan sebagai member
     */
    public Pelanggan tambahMember(String nama, String noTelepon, String email, String alamat) {
        // Generate kode member
        String kodeMember = pelangganRepository.generateKodeMember();
        
        Pelanggan pelanggan = new Pelanggan(kodeMember, nama, noTelepon);
        pelanggan.setEmail(email);
        pelanggan.setAlamat(alamat);
        pelanggan.setTipeMember("REGULAR");
        
        Pelanggan saved = pelangganRepository.save(pelanggan);
        logger.info("Member baru berhasil ditambahkan: {}", kodeMember);
        
        return saved;
    }
    
    /**
     * Update pelanggan
     */
    public Pelanggan updatePelanggan(Pelanggan pelanggan) {
        return pelangganRepository.update(pelanggan);
    }
    
    /**
     * Hapus pelanggan (soft delete)
     */
    public void hapusPelanggan(Long pelangganId) {
        Optional<Pelanggan> pelangganOpt = pelangganRepository.findById(pelangganId);
        if (pelangganOpt.isPresent()) {
            Pelanggan pelanggan = pelangganOpt.get();
            pelanggan.setAktif(false);
            pelangganRepository.update(pelanggan);
            logger.info("Pelanggan berhasil dinonaktifkan: {}", pelanggan.getNama());
        }
    }
    
    /**
     * Cari pelanggan berdasarkan ID
     */
    public Optional<Pelanggan> getPelangganById(Long id) {
        return pelangganRepository.findById(id);
    }
    
    /**
     * Cari pelanggan berdasarkan kode member
     */
    public Optional<Pelanggan> getPelangganByKodeMember(String kodeMember) {
        return pelangganRepository.findByKodeMember(kodeMember);
    }
    
    /**
     * Cari pelanggan berdasarkan no telepon
     */
    public Optional<Pelanggan> getPelangganByNoTelepon(String noTelepon) {
        return pelangganRepository.findByNoTelepon(noTelepon);
    }
    
    /**
     * Dapatkan semua pelanggan aktif
     */
    public List<Pelanggan> getAllPelangganAktif() {
        return pelangganRepository.findAllActive();
    }
    
    /**
     * Dapatkan semua member
     */
    public List<Pelanggan> getAllMember() {
        return pelangganRepository.findMembers();
    }
    
    /**
     * Cari pelanggan
     */
    public List<Pelanggan> cariPelanggan(String keyword) {
        return pelangganRepository.searchByKeyword(keyword);
    }
    
    /**
     * Dapatkan pelanggan berdasarkan tipe member
     */
    public List<Pelanggan> getPelangganByTipeMember(String tipeMember) {
        return pelangganRepository.findByTipeMember(tipeMember);
    }
    
    /**
     * Tambah poin pelanggan
     */
    public void tambahPoin(Long pelangganId, int poin) {
        Optional<Pelanggan> pelangganOpt = pelangganRepository.findById(pelangganId);
        if (pelangganOpt.isPresent()) {
            Pelanggan pelanggan = pelangganOpt.get();
            pelanggan.tambahPoin(poin);
            pelangganRepository.update(pelanggan);
            logger.info("Poin ditambahkan untuk pelanggan {}: +{}", pelanggan.getNama(), poin);
        }
    }
    
    /**
     * Kurangi poin pelanggan (untuk redeem)
     */
    public boolean kurangiPoin(Long pelangganId, int poin) {
        Optional<Pelanggan> pelangganOpt = pelangganRepository.findById(pelangganId);
        if (pelangganOpt.isPresent()) {
            Pelanggan pelanggan = pelangganOpt.get();
            if (pelanggan.kurangiPoin(poin)) {
                pelangganRepository.update(pelanggan);
                logger.info("Poin dikurangi untuk pelanggan {}: -{}", pelanggan.getNama(), poin);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Update total belanja pelanggan setelah transaksi
     */
    public void updateTotalBelanja(Long pelangganId, BigDecimal nominal) {
        pelangganRepository.updateTotalBelanja(pelangganId, nominal);
    }
    
    /**
     * Hitung poin dari nominal belanja (1 poin per 10.000)
     */
    public int hitungPoin(BigDecimal nominal) {
        return nominal.divide(new BigDecimal("10000"), 0, java.math.RoundingMode.DOWN).intValue();
    }
    
    /**
     * Dapatkan pelanggan dengan poin tertinggi
     */
    public List<Pelanggan> getTopPelangganByPoin(int limit) {
        return pelangganRepository.findTopByPoin(limit);
    }
    
    /**
     * Dapatkan pelanggan dengan total belanja tertinggi
     */
    public List<Pelanggan> getTopPelangganByBelanja(int limit) {
        return pelangganRepository.findTopByTotalBelanja(limit);
    }
    
    /**
     * Hitung total member
     */
    public long countMember() {
        return pelangganRepository.countMembers();
    }
    
    /**
     * Generate kode member baru
     */
    public String generateKodeMember() {
        return pelangganRepository.generateKodeMember();
    }
    
    /**
     * Upgrade pelanggan biasa menjadi member
     */
    public Pelanggan upgradeTOMember(Long pelangganId) {
        Optional<Pelanggan> pelangganOpt = pelangganRepository.findById(pelangganId);
        if (pelangganOpt.isEmpty()) {
            throw new IllegalArgumentException("Pelanggan tidak ditemukan");
        }
        
        Pelanggan pelanggan = pelangganOpt.get();
        if (pelanggan.isMember()) {
            throw new IllegalStateException("Pelanggan sudah menjadi member");
        }
        
        pelanggan.setKodeMember(pelangganRepository.generateKodeMember());
        pelanggan.setTipeMember("REGULAR");
        
        return pelangganRepository.update(pelanggan);
    }
}
