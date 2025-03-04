package sp25.swp391.se1809.group4.bookstore.controllers;

import sp25.swp391.se1809.group4.bookstore.daos.ImportStockDetailDAO;
import sp25.swp391.se1809.group4.bookstore.models.ImportStockDTO;
import sp25.swp391.se1809.group4.bookstore.models.ImportStockDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/importStock")
@CrossOrigin(origins = "http://localhost:5173")
public class ImportStockDetailController {

    private final ImportStockDetailDAO importStockDetailDAO;

    @Autowired
    public ImportStockDetailController(ImportStockDetailDAO importStockDetailDAO) {
        this.importStockDetailDAO = importStockDetailDAO;
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getImportStockDetailsByStockId(@PathVariable int id) {
        try {
            List<ImportStockDetailDTO> details = importStockDetailDAO.findByImportStockId(id);

            if (details == null || details.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No details found for import stock ID: " + id);
            }

            return ResponseEntity.ok(details);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching details: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/details")
    public ResponseEntity<?> addImportStockDetails(@PathVariable int id, @RequestBody List<ImportStockDetailDTO> details) {
        try {
            // Tạo đối tượng ImportStockDTO với ID được truyền vào
            ImportStockDTO importStock = new ImportStockDTO();
            importStock.setIsid(id); // Gán ID từ đường dẫn

            for (ImportStockDetailDTO detail : details) {
                // Gán isid cho từng ImportStockDetailDTO
                detail.setIsid(importStock);

                System.out.println("Detail to save with isid: " + detail);
                importStockDetailDAO.save(detail); // Lưu vào database
            }

            return ResponseEntity.ok("Details added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }







}
