package com.joko.aplikasijava.repositories;

import com.joko.aplikasijava.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Generic Repository untuk operasi CRUD dasar
 * Mengimplementasikan pattern Repository untuk abstraksi data access
 * @param <T> Entity type
 * @param <ID> Primary key type
 */
public abstract class GenericRepository<T, ID extends Serializable> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final SessionFactory sessionFactory;
    protected final Class<T> entityClass;
    
    public GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }
    
    /**
     * Menyimpan entity baru
     */
    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            logger.debug("Saved entity: {}", entity);
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving entity: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menyimpan data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update entity yang sudah ada
     */
    public T update(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            T mergedEntity = session.merge(entity);
            transaction.commit();
            logger.debug("Updated entity: {}", mergedEntity);
            return mergedEntity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal mengupdate data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Simpan atau update entity
     */
    public T saveOrUpdate(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            T mergedEntity = session.merge(entity);
            transaction.commit();
            return mergedEntity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving/updating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menyimpan/mengupdate data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Hapus entity
     */
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            T mergedEntity = session.merge(entity);
            session.remove(mergedEntity);
            transaction.commit();
            logger.debug("Deleted entity: {}", entity);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menghapus data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Hapus entity berdasarkan ID
     */
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
    
    /**
     * Cari entity berdasarkan ID
     */
    public Optional<T> findById(ID id) {
        try (Session session = sessionFactory.openSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error("Error finding entity by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Gagal mencari data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Dapatkan semua entity
     */
    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            Query<T> query = session.createQuery(hql, entityClass);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal mengambil semua data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Dapatkan semua entity dengan pagination
     */
    public List<T> findAll(int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            Query<T> query = session.createQuery(hql, entityClass);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding entities with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal mengambil data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Hitung total entity
     */
    public long count() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting entities: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menghitung data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cek apakah entity dengan ID tertentu ada
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
    
    /**
     * Execute query dengan parameter
     */
    protected List<T> executeQuery(String hql, Object... params) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i += 2) {
                query.setParameter((String) params[i], params[i + 1]);
            }
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error executing query: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menjalankan query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute single result query
     */
    protected Optional<T> executeSingleQuery(String hql, Object... params) {
        try (Session session = sessionFactory.openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i += 2) {
                query.setParameter((String) params[i], params[i + 1]);
            }
            query.setMaxResults(1);
            List<T> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error executing single query: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menjalankan query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute update/delete query
     */
    protected int executeUpdate(String hql, Object... params) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createMutationQuery(hql);
            for (int i = 0; i < params.length; i += 2) {
                query.setParameter((String) params[i], params[i + 1]);
            }
            int result = query.executeUpdate();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error executing update: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menjalankan update: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get session for complex operations
     */
    protected Session getSession() {
        return sessionFactory.openSession();
    }
}
