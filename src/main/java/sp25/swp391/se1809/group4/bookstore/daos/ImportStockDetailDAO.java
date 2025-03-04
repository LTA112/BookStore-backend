package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.ImportStockDetailDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImportStockDetailDAO {

    private final EntityManager entityManager;

    @Autowired
    public ImportStockDetailDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<ImportStockDetailDTO> findByImportStockId(int importStockId) {
        TypedQuery<ImportStockDetailDTO> query = entityManager.createQuery(
                "SELECT d FROM ImportStockDetailDTO d JOIN FETCH d.bookID WHERE d.isid.isid = :importStockId", ImportStockDetailDTO.class);
        query.setParameter("importStockId", importStockId);
        return query.getResultList();
    }

    @Transactional
    public void save(ImportStockDetailDTO importStockDetailDTO) {
        entityManager.persist(importStockDetailDTO);
        entityManager.flush();  // Optional, ensures immediate write to DB
    }

}
