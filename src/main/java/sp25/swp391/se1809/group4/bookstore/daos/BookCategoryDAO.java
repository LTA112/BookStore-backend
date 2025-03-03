package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.BookCategoryDTO;

import java.util.List;

public interface BookCategoryDAO {
    void save(BookCategoryDTO bookCategoryDTO);
    BookCategoryDTO find(int bookCateId);
    void update(BookCategoryDTO bookCategoryDTO);
    void delete(int bookCateId);
    List<BookCategoryDTO> findAll();
    List<BookCategoryDTO> findByBookId(int bookId); // Tìm các danh mục của một sách
}
