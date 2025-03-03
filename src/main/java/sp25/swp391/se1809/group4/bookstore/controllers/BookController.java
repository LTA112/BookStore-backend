package sp25.swp391.se1809.group4.bookstore.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import sp25.swp391.se1809.group4.bookstore.daos.BookDAO;
import sp25.swp391.se1809.group4.bookstore.models.BookCategoryDTO;
import sp25.swp391.se1809.group4.bookstore.models.BookDTO;
import sp25.swp391.se1809.group4.bookstore.models.CategoryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("api/v1/books")
@CrossOrigin(origins = "http://localhost:5173")
public class BookController {
    private final BookDAO bookDAO;

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public BookController(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }


    @GetMapping("/")
    @Transactional
    public List<BookDTO> getBooksList() {
        List<BookDTO> books = bookDAO.findAll();
        // Khởi tạo `bookCategories` và các `CategoryDTO` liên quan
        books.forEach(book -> {
            Hibernate.initialize(book.getBookCategories());
            book.getBookCategories().forEach(bookCategory -> {
                Hibernate.initialize(bookCategory.getCatId());
            });
        });
        return books;
    }

    @GetMapping("/search")
    @Transactional
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String keyword) {
        try {
            List<BookDTO> books = bookDAO.searchBooks(keyword);

            // Khởi tạo các liên kết `bookCategories` và `CategoryDTO` để tránh lỗi lazy initialization
            books.forEach(book -> {
                Hibernate.initialize(book.getBookCategories());
                book.getBookCategories().forEach(bookCategory -> {
                    Hibernate.initialize(bookCategory.getCatId());
                });
            });

            return ResponseEntity.ok(books);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/sort")
    @Transactional
    public ResponseEntity<List<BookDTO>> sortBooks(
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        try {
            List<BookDTO> sortedBooks = bookDAO.sortBooks(sortBy, sortOrder);
            return ResponseEntity.ok(sortedBooks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category")
    @Transactional
    public ResponseEntity<List<BookDTO>> filterBooksByCategory(@RequestParam String categoryID) {
        System.out.println("categoryID " + categoryID);
        List<BookDTO> books = bookDAO.filterBooksByCategory(Integer.parseInt(categoryID));
        if (!books.isEmpty()) {
            return ResponseEntity.ok(books);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/{bookId}")
    @Transactional
    public ResponseEntity<BookDTO> getBook(@PathVariable int bookId) {
        BookDTO book = bookDAO.find(bookId);
        if (book != null) {
            // Khởi tạo `bookCategories` và `CategoryDTO`
            Hibernate.initialize(book.getBookCategories());
            book.getBookCategories().forEach(bookCategory -> {
                Hibernate.initialize(bookCategory.getCatId());
            });
            return ResponseEntity.ok(book);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }



    @PostMapping
    @Transactional
    public ResponseEntity<BookDTO> addBook(
            @RequestPart("book") String bookData,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BookDTO book = objectMapper.readValue(bookData, BookDTO.class);
            book.setBookStatus(1);

            List<BookDTO> existingBooks = bookDAO.findBooksByTitleAndAuthorAndPublisher(
                    book.getBookTitle(), book.getAuthor(), book.getPublisher());
            if (!existingBooks.isEmpty()) {
                BookDTO existingBook = existingBooks.get(0);
                existingBook.setBookQuantity(
                        (existingBook.getBookQuantity() == null ? 0 : existingBook.getBookQuantity())
                                + (book.getBookQuantity() == null ? 0 : book.getBookQuantity())
                );
                bookDAO.update(existingBook);
                return ResponseEntity.ok(existingBook);
            }
            bookDAO.save(book);

            if (book.getBookCategories() != null && !book.getBookCategories().isEmpty()) {
                for (BookCategoryDTO bookCategory : book.getBookCategories()) {
                    CategoryDTO category = entityManager.find(CategoryDTO.class, bookCategory.getCatId().getCatID());
                    if (category == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    bookCategory.setBookId(book);
                    entityManager.persist(bookCategory);
                }
            }

            if (image != null && !image.isEmpty()) {
                String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
                String uniqueFileName = "book_" + book.getBookID() + "_" + System.currentTimeMillis() + ".jpg";
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                try (InputStream inputStream = image.getInputStream()) {
                    Path targetPath = uploadPath.resolve(uniqueFileName);
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                book.setImage("/uploads/" + uniqueFileName);
                bookDAO.update(book);
            }

            Hibernate.initialize(book.getBookCategories()); // Khởi tạo thủ công trước khi trả về
            return ResponseEntity.status(HttpStatus.CREATED).body(book);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/{bookId}/status")
    @Transactional
    public ResponseEntity<BookDTO> updateBookStatus(@PathVariable int bookId, @RequestParam int status) {
        BookDTO existingBook = bookDAO.find(bookId);
        if (existingBook == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        existingBook.setBookStatus(status); // Chỉ cập nhật trạng thái
        bookDAO.update(existingBook);
        return ResponseEntity.ok(existingBook);
    }


    @PutMapping("/{bookId}")
    @Transactional
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable int bookId,
            @RequestPart("book") String bookData,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BookDTO updatedBook = objectMapper.readValue(bookData, BookDTO.class);

            // Lấy sách hiện tại từ database
            BookDTO existingBook = bookDAO.find(bookId);
            if (existingBook == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            // Xóa các BookCategoryDTO cũ
            // Xóa các BookCategoryDTO cũ
            List<BookCategoryDTO> oldCategories = existingBook.getBookCategories();
            if (oldCategories != null) {
                for (BookCategoryDTO bookCategory : oldCategories) {
                    entityManager.remove(entityManager.contains(bookCategory) ? bookCategory : entityManager.merge(bookCategory));
                }
                oldCategories.clear(); // Làm sạch danh sách trong thực thể
            }
            entityManager.flush(); // Đảm bảo các thực thể cũ được xóa hoàn toàn


            // Thêm danh sách BookCategoryDTO mới
            if (updatedBook.getBookCategories() != null && !updatedBook.getBookCategories().isEmpty()) {
                for (BookCategoryDTO bookCategory : updatedBook.getBookCategories()) {
                    CategoryDTO category = entityManager.find(CategoryDTO.class, bookCategory.getCatId().getCatID());
                    if (category == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Danh mục không tồn tại
                    }
                    bookCategory.setBookId(existingBook); // Liên kết với sách hiện tại
                    entityManager.merge(bookCategory); // Sử dụng merge thay vì persist
                }
            }



            // Cập nhật thông tin sách
            existingBook.setBookTitle(updatedBook.getBookTitle());
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setPublisher(updatedBook.getPublisher());
            existingBook.setPublicationYear(updatedBook.getPublicationYear());
            existingBook.setTranslator(updatedBook.getTranslator());
            existingBook.setHardcover(updatedBook.getHardcover());
            existingBook.setDimension(updatedBook.getDimension());
            existingBook.setWeight(updatedBook.getWeight());
            existingBook.setBookPrice(updatedBook.getBookPrice());
            existingBook.setBookDescription(updatedBook.getBookDescription());
            existingBook.setBookStatus(updatedBook.getBookStatus());
            existingBook.setBookQuantity(updatedBook.getBookQuantity());
            existingBook.setIsbn(updatedBook.getIsbn());

            // Xử lý hình ảnh nếu có
            if (image != null && !image.isEmpty()) {
                String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
                String uniqueFileName = "book_" + bookId + "_" + System.currentTimeMillis() + ".jpg";
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                try (InputStream inputStream = image.getInputStream()) {
                    Path targetPath = uploadPath.resolve(uniqueFileName);
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                existingBook.setImage("/uploads/" + uniqueFileName);
            }

            // Cập nhật sách
            bookDAO.update(existingBook);

            Hibernate.initialize(existingBook.getBookCategories()); // Khởi tạo thủ công trước khi trả về
            return ResponseEntity.ok(existingBook);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable int bookId) {
        BookDTO book = bookDAO.find(bookId);
        if (book != null) {
            bookDAO.delete(bookId);
            return ResponseEntity.ok("Book deleted successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
    }

}
