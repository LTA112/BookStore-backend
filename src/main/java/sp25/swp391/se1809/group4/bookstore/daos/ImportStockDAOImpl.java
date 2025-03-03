package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.ImportStockDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImportStockDAOImpl implements ImportStockDAO  {
    private final EntityManager entityManager;

    @Autowired
    public ImportStockDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ImportStockDTO save(ImportStockDTO importStockDTO) {
        entityManager.persist(importStockDTO);
        return importStockDTO;  // Return the saved entity
    }

    @Override
    public ImportStockDTO find(int ISID) {
        return entityManager.find(ImportStockDTO.class, ISID);
    }

    @Override
    public void update(ImportStockDTO importStockDTO) {
        entityManager.merge(importStockDTO);
    }

    @Override
    public void delete(int ISID) {
        entityManager.remove(this.find(ISID));
    }

    @Override
    public List<ImportStockDTO> findAll() {
        TypedQuery<ImportStockDTO> query = entityManager.createQuery("From ImportStockDTO", ImportStockDTO.class);
        return query.getResultList();
    }
}

