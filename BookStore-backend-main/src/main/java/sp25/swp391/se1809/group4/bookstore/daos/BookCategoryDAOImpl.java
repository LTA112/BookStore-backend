package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.BookCategoryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class BookCategoryDAOImpl implements BookCategoryDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(BookCategoryDTO bookCategoryDTO) {
        entityManager.persist(bookCategoryDTO);
    }

    @Override
    public BookCategoryDTO find(int bookCateId) {
        return entityManager.find(BookCategoryDTO.class, bookCateId);
    }

    @Override
    public void update(BookCategoryDTO bookCategoryDTO) {
        entityManager.merge(bookCategoryDTO);
    }

    @Override
    public void delete(int bookCateId) {
        BookCategoryDTO bookCategory = find(bookCateId);
        if (bookCategory != null) {
            entityManager.remove(bookCategory);
        }
    }

    @Override
    public List<BookCategoryDTO> findAll() {
        return entityManager.createQuery("SELECT bc FROM BookCategoryDTO bc", BookCategoryDTO.class).getResultList();
    }

    @Override
    public List<BookCategoryDTO> findByBookId(int bookId) {
        return entityManager.createQuery(
                        "SELECT bc FROM BookCategoryDTO bc WHERE bc.bookId.bookID = :bookId", BookCategoryDTO.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }
}
