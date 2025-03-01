package sp25.swp391.se1809.group4.bookstore.controllers;

import sp25.swp391.se1809.group4.bookstore.daos.BookCategoryDAO;
import sp25.swp391.se1809.group4.bookstore.models.BookCategoryDTO;
import sp25.swp391.se1809.group4.bookstore.models.BookDTO;
import sp25.swp391.se1809.group4.bookstore.models.CategoryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class BookCategoryController {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BookCategoryDAO bookCategoryDAO;

    @PostMapping("/add-category")
    @Transactional
    public ResponseEntity<String> addBookCategory(@RequestParam int bookId, @RequestParam int catId) {
        // Tìm sách và thể loại
        BookDTO book = entityManager.find(BookDTO.class, bookId);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found with ID: " + bookId);
        }

        CategoryDTO category = entityManager.find(CategoryDTO.class, catId);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found with ID: " + catId);
        }

        // Tạo BookCategoryDTO và lưu vào database
        BookCategoryDTO bookCategory = new BookCategoryDTO();
        bookCategory.setBookId(book);
        bookCategory.setCatId(category);
        bookCategoryDAO.save(bookCategory);

        return ResponseEntity.status(HttpStatus.CREATED).body("BookCategory created successfully!");
    }

    @GetMapping("/{bookId}/categories")
    public ResponseEntity<List<BookCategoryDTO>> getCategoriesByBook(@PathVariable int bookId) {
        List<BookCategoryDTO> categories = bookCategoryDAO.findByBookId(bookId);
        if (categories.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(categories);
    }

    @DeleteMapping("/delete-category/{bookCateId}")
    @Transactional
    public ResponseEntity<String> deleteBookCategory(@PathVariable int bookCateId) {
        BookCategoryDTO bookCategory = bookCategoryDAO.find(bookCateId);
        if (bookCategory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("BookCategory not found with ID: " + bookCateId);
        }
        bookCategoryDAO.delete(bookCateId);
        return ResponseEntity.ok("BookCategory deleted successfully!");
    }

}
