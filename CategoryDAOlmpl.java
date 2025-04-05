package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.exceptions.CategoryExceptionNotFound;
import sp25.swp391.se1809.group4.bookstore.models.CategoryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CategoryDAOlmpl implements CategoryDAO {

    private final EntityManager entityManager;

    @Autowired
    public CategoryDAOlmpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public CategoryDTO save(CategoryDTO categoryDTO) {
        String name = categoryDTO.getCatName();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        return entityManager.merge(categoryDTO);
    }

    @Override
    public CategoryDTO find(int catID) {
        CategoryDTO object = entityManager.find(CategoryDTO.class, catID);
        if (object == null) {
            throw new CategoryExceptionNotFound();
        }
        return object;
    }

    @Override
    @Transactional
    public void delete(int catID) {
        CategoryDTO object = find(catID);
        entityManager.remove(object);
    }

    @Override
    public List<CategoryDTO> findAll() {
        return entityManager
                .createQuery("SELECT c FROM CategoryDTO c", CategoryDTO.class)
                .getResultList();
    }

    @Override
    public List<CategoryDTO> searchByName(String name) {
        String pattern = "%" + name.trim().toLowerCase() + "%";

        return entityManager
                .createQuery(
                        "SELECT c FROM CategoryDTO c WHERE LOWER(c.catName) LIKE :name",
                        CategoryDTO.class
                )
                .setParameter("name", pattern)
                .getResultList();
    }
}
