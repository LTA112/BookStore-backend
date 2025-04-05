package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.BookDTO;
import sp25.swp391.se1809.group4.bookstore.models.CategoryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class BookDAOImpl implements BookDAO {

    private final EntityManager entityManager;

    @Autowired
    public BookDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(BookDTO bookDTO) {
        if (bookDTO.getBookID() == null) {
            entityManager.persist(bookDTO);
        } else {
            entityManager.merge(bookDTO);
        }
    }

    @Override
    public BookDTO find(int bookId) {
        BookDTO book = entityManager.find(BookDTO.class, bookId);
        if (book == null) {
            System.out.printf("Book not found with ID: %d%n", bookId);
        } else {
            System.out.printf("Book found with ID: %d%n", bookId);
        }
        return book;
    }

    @Override
    @Transactional
    public void update(BookDTO book) {
        BookDTO existingBook = entityManager.find(BookDTO.class, book.getBookID());
        if (existingBook == null) {
            throw new IllegalArgumentException("Book does not exist in the database.");
        }
        entityManager.merge(book);
    }

    @Override
    @Transactional
    public void delete(int bookID) {
        BookDTO book = find(bookID);
        if (book != null) {
            entityManager.remove(book);
        }
    }

    @Override
    public List<BookDTO> findAll() {
        return entityManager
                .createQuery("SELECT b FROM BookDTO b", BookDTO.class)
                .getResultList();
    }

    @Override
    public List<BookDTO> sortBooks(String sortBy, String sortOrder) {
        String direction = "ASC".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        String queryString = "SELECT b FROM BookDTO b WHERE b.bookStatus = 1";

        switch (sortBy.toLowerCase()) {
            case "price":
                queryString += " ORDER BY b.bookPrice " + direction;
                break;
            case "title":
                queryString += " ORDER BY b.bookTitle " + direction;
                break;
            default:
                break;
        }

        return entityManager
                .createQuery(queryString, BookDTO.class)
                .getResultList();
    }

    @Override
    public List<BookDTO> filterBooksByCategory(int categoryID) {
        System.out.println("Category ID: " + categoryID);

        CategoryDAOlmpl dao = new CategoryDAOlmpl(entityManager);
        CategoryDTO category = dao.find(categoryID);

        List<BookDTO> result = entityManager
                .createQuery(
                        "SELECT b FROM BookDTO b JOIN FETCH b.bookCategories c WHERE c.catId = :category",
                        BookDTO.class)
                .setParameter("category", category)
                .getResultList();

        System.out.println("Books found: " + result.size());
        return result;
    }

    @Override
    public List<BookDTO> searchBooks(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";

        TypedQuery<BookDTO> query = entityManager.createQuery(
                "SELECT b FROM BookDTO b WHERE " +
                        "LOWER(b.bookTitle) LIKE :searchTerm OR " +
                        "LOWER(b.author) LIKE :searchTerm OR " +
                        "LOWER(b.publisher) LIKE :searchTerm",
                BookDTO.class);

        query.setParameter("searchTerm", pattern);
        return query.getResultList();
    }

    @Override
    public List<BookDTO> findBooksByTitleAndAuthorAndPublisher(String bookTitle, String author, String publisher) {
        System.out.printf("Searching with title: %s, author: %s, publisher: %s%n", bookTitle, author, publisher);

        List<BookDTO> result = entityManager
                .createQuery(
                        "SELECT b FROM BookDTO b WHERE b.bookTitle = :bookTitle AND b.author = :author AND b.publisher = :publisher",
                        BookDTO.class)
                .setParameter("bookTitle", bookTitle)
                .setParameter("author", author)
                .setParameter("publisher", publisher)
                .getResultList();

        System.out.println("Found books: " + result.size());
        return result;
    }
}
