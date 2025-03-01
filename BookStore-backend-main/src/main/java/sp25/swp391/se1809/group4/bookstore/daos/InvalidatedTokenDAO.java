package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.InvalidatedTokenDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class InvalidatedTokenDAO {

    EntityManager entityManager;

    @Autowired
    public InvalidatedTokenDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void save(InvalidatedTokenDTO token) {
        String jpql = "INSERT INTO InvalidatedTokenDTO (ITID, expiryTime) "
                + "VALUES ( :id, :expiryTime)";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", token.getITID());
        query.setParameter("expiryTime", token.getExpiryTime());

        query.executeUpdate();
    }

    public boolean existsById(String jwtid) {
        String jpql = "FROM InvalidatedTokenDTO WHERE id=:id";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", jwtid);
        List result = query.getResultList();
        return result.size() > 0;
    }
}
