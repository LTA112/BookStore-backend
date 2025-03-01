package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.CategoryDTO;
import java.util.List;

public interface CategoryDAO {
    CategoryDTO save(CategoryDTO category);
    CategoryDTO find(int catID);
    void delete(int catID);
    List<CategoryDTO> findAll();

    // Thêm phương thức này để tìm kiếm category theo tên
    List<CategoryDTO> searchByName(String name);
}
