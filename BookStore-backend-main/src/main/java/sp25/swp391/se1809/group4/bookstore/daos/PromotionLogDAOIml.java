package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.PromotionLogDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public class PromotionLogDAOIml implements PromotionLogDAO {
    private final EntityManager entityManager;

    @Autowired
    public PromotionLogDAOIml(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(PromotionLogDTO promotionLogDTO) {
        entityManager.merge(promotionLogDTO);
    }

    @Override
    public List<PromotionLogDTO> findAll() {
        TypedQuery<PromotionLogDTO> query = entityManager.createQuery("SELECT p FROM PromotionLogDTO p", PromotionLogDTO.class);
        return query.getResultList();
    }

    @Override
    public List<PromotionLogDTO> findByAction(Integer action) {
        String jpql = "SELECT p FROM PromotionLogDTO p WHERE p.proAction = :action";
        TypedQuery<PromotionLogDTO> query = entityManager.createQuery(jpql, PromotionLogDTO.class);
        query.setParameter("action", action);
        return query.getResultList();
    }

    @Override
    public List<PromotionLogDTO> findByDateRange(Date startDate, Date endDate) {
        String jpql = "SELECT p FROM PromotionLogDTO p WHERE p.proLogDate BETWEEN :startDate AND :endDate";
        TypedQuery<PromotionLogDTO> query = entityManager.createQuery(jpql, PromotionLogDTO.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
}
