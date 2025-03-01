package sp25.swp391.se1809.group4.bookstore.controllers;

import sp25.swp391.se1809.group4.bookstore.daos.ImportStockDAO;
import sp25.swp391.se1809.group4.bookstore.daos.ImportStockDetailDAO;
import sp25.swp391.se1809.group4.bookstore.models.ImportStockDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/importStock")
@CrossOrigin(origins = "http://localhost:5173")
public class ImportStockController {
    private final ImportStockDAO importStockDAO;
    private final ImportStockDetailDAO importStockDetailDAO;

    @Autowired
    public ImportStockController(ImportStockDAO importStockDAO, ImportStockDetailDAO importStockDetailDAO) {
        this.importStockDAO = importStockDAO;
        this.importStockDetailDAO = importStockDetailDAO;
    }

    @GetMapping("/")
    public ResponseEntity<List<ImportStockDTO>> getStocksList() {
        List<ImportStockDTO> stocks = importStockDAO.findAll();
        return ResponseEntity.ok(stocks);
    }

    @PostMapping("/")
    @Transactional
    public ResponseEntity<ImportStockDTO> createImportStock(@RequestBody ImportStockDTO importStockDTO) {
        // Lưu thông tin ImportStock
        ImportStockDTO savedStock = importStockDAO.save(importStockDTO);

        // Kiểm tra danh sách chi tiết nhập kho trước khi lưu
        if (importStockDTO.getImportStockDetailCollection() != null && !importStockDTO.getImportStockDetailCollection().isEmpty()) {
            importStockDTO.getImportStockDetailCollection().forEach(detail -> {
                detail.setIsid(savedStock);  // Gán liên kết với ImportStock
                importStockDetailDAO.save(detail); // Lưu từng chi tiết vào cơ sở dữ liệu
            });
        }

        return ResponseEntity.ok(savedStock);
    }


}
