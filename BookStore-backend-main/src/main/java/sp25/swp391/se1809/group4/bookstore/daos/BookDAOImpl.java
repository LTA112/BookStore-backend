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
public class BookDAOImpl implements BookDAO{
    EntityManager entityManager;

    @Autowired
    public BookDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    @Transactional
    public void save(BookDTO bookDTO) {
        if (bookDTO.getBookID() != null) {
            entityManager.merge(bookDTO);  // Sử dụng merge cho đối tượng đã tồn tại
        } else {
            entityManager.persist(bookDTO);  // Sử dụng persist cho đối tượng mới
        }
    }
    @Override
    public BookDTO find(int bookId) {
        System.out.println("Finding book with ID: " + bookId);
        BookDTO book = entityManager.find(BookDTO.class, bookId);
        if (book == null) {
            System.out.println("Book not found in database with ID: " + bookId);
        }
        return book;
    }


    @Override
    @Transactional
    public void update(BookDTO book) {
        BookDTO existingBook = entityManager.find(BookDTO.class, book.getBookID());
        if (existingBook != null) {
            entityManager.merge(book); // Chỉ merge nếu thực thể chưa bị xóa
        } else {
            throw new IllegalArgumentException("Book does not exist in the database.");
        }
    }

    @Override
    @Transactional
    public void delete(int bookID) {
        entityManager.remove(this.find(bookID));
    }

    @Override
    public List<BookDTO> findAll() {
        TypedQuery<BookDTO> query = entityManager.createQuery("From BookDTO", BookDTO.class);
        return query.getResultList();
    }

    @Override
    public List<BookDTO> sortBooks(String sortBy, String sortOrder) {
        String queryString = "FROM BookDTO b WHERE b.bookStatus = 1"; // Chỉ lấy sách hợp lệ
        if ("price".equalsIgnoreCase(sortBy)) {
            queryString += " ORDER BY b.bookPrice " + ("desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC");
        } else if ("title".equalsIgnoreCase(sortBy)) {
            queryString += " ORDER BY b.bookTitle " + ("desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC");
        }
        TypedQuery<BookDTO> query = entityManager.createQuery(queryString, BookDTO.class);
        return query.getResultList();
    }

    @Override
    public List<BookDTO> filterBooksByCategory(int categoryID) {
        System.out.println("Category ID: " + categoryID);
        CategoryDAOlmpl dao = new CategoryDAOlmpl(entityManager);
        CategoryDTO category = dao.find(categoryID);
        List<BookDTO> result =  entityManager.createQuery("SELECT b FROM BookDTO b JOIN FETCH b.bookCategories c WHERE c.catId = :category", BookDTO.class)
                .setParameter("category", category)
                .getResultList();
        System.out.println(result.size());
        return result;
    }

    @Override
    public List<BookDTO> searchBooks(String searchTerm) {
        String jpql = "FROM BookDTO WHERE " +
                "LOWER(bookTitle) LIKE :searchTerm OR " +
                "LOWER(author) LIKE :searchTerm OR " +
                "LOWER(publisher) LIKE :searchTerm";
        TypedQuery<BookDTO> query = entityManager.createQuery(jpql, BookDTO.class);
        query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
        return query.getResultList();
    }


    @Override
    public List<BookDTO> findBooksByTitleAndAuthorAndPublisher(String bookTitle, String author, String publisher) {
        System.out.println("Searching for book with title: " + bookTitle + ", author: " + author + ", publisher: " + publisher);
        List<BookDTO> result = entityManager.createQuery(
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
