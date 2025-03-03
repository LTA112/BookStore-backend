package sp25.swp391.se1809.group4.bookstore.controllers;


import sp25.swp391.se1809.group4.bookstore.daos.CategoryDAO;
import sp25.swp391.se1809.group4.bookstore.exceptions.CategoryExceptionNotFound;
import sp25.swp391.se1809.group4.bookstore.exceptions.CategoryExceptionResponseDTO;
import sp25.swp391.se1809.group4.bookstore.models.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@EnableWebSecurity
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    CategoryDAO categoryDAO;

    @Autowired
    public CategoryController(CategoryDAO categoryDAO) {
            this.categoryDAO = categoryDAO;
        }

        @GetMapping("/")
        public List<CategoryDTO> getCategoriesList(){
            return categoryDAO.findAll();
        }



    @GetMapping("/{catID}")
    public CategoryDTO getCategory(@PathVariable int catID){
        if(categoryDAO.find(catID)!=null)
            return categoryDAO.find(catID);
        else throw new CategoryExceptionNotFound();
    }


    @ExceptionHandler
    public ResponseEntity<CategoryExceptionResponseDTO> handlerException(CategoryExceptionNotFound exec){
        CategoryExceptionResponseDTO error = new CategoryExceptionResponseDTO();
        error.setStatus(404);
        error.setMessage(exec.getMessage());
        error.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/")
    public CategoryDTO addCategory(@RequestBody CategoryDTO category){
        System.out.println("Received category data: " + category); // Log dữ liệu category được nhận
        return categoryDAO.save(category);
    }


    @PutMapping("/{catID}")
    public CategoryDTO updateCategory(@PathVariable int catID, @RequestBody CategoryDTO category) {
        CategoryDTO existingCategory = categoryDAO.find(catID);
        if (existingCategory == null) {
            throw new CategoryExceptionNotFound();
        }

        // Đảm bảo catStatus không null và đặt giá trị mặc định nếu cần
        existingCategory.setCatStatus(category.getCatStatus() != null ? category.getCatStatus() : 1);

        // Cập nhật thông tin khác
        existingCategory.setCatName(category.getCatName());
        existingCategory.setCatDescription(category.getCatDescription());
        return categoryDAO.save(existingCategory);
    }



    @PutMapping("/{catID}/soft-delete")
    public ResponseEntity<?> softDeleteCategory(@PathVariable int catID) {
        // Tìm category theo ID
        CategoryDTO category = categoryDAO.find(catID);

        // Kiểm tra nếu không tìm thấy category
        if (category == null) {
            throw new CategoryExceptionNotFound();
        }


        // Thực hiện soft delete (set catStatus = 0)
        category.setCatStatus(0);
        categoryDAO.save(category);

        return ResponseEntity.ok("Category soft deleted successfully!");
    }


    @GetMapping("/search")
    public List<CategoryDTO> searchCategories(@RequestParam(required = false) Integer id,
                                              @RequestParam(required = false) String name) {
        // Tìm kiếm theo ID nếu chỉ có ID
        if (id != null && (name == null || name.isEmpty())) {
            try {
                CategoryDTO category = categoryDAO.find(id);
                return category != null ? List.of(category) : List.of();
            } catch (CategoryExceptionNotFound e) {
                return List.of(); // Nếu không tìm thấy, trả về danh sách rỗng
            }
        }

        // Tìm kiếm theo Name nếu chỉ có Name
        if (name != null && !name.isEmpty() && id == null) {
            return categoryDAO.searchByName(name);
        }

        // Nếu cả ID và Name đều có
        if (id != null && name != null && !name.isEmpty()) {
            List<CategoryDTO> results = categoryDAO.searchByName(name);
            return results.stream()
                    .filter(category -> category.getCatID() == id) // Lọc thêm theo ID
                    .toList();
        }

        // Trường hợp không có tham số, trả về danh sách rỗng
        return List.of();
    }
}
