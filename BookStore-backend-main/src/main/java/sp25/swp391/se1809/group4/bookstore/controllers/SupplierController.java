package sp25.swp391.se1809.group4.bookstore.controllers;

import sp25.swp391.se1809.group4.bookstore.daos.SupplierDAO;
import sp25.swp391.se1809.group4.bookstore.models.SupplierDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/suppliers")
public class SupplierController {
    @Autowired
    private final SupplierDAO supplierDAO;

    @Autowired
    public SupplierController(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public List<SupplierDTO> getSuppliersList() {
        return supplierDAO.findAll();
    }

    @GetMapping("/{supID}")
    public ResponseEntity<SupplierDTO> getSupplier(@PathVariable Integer supID) {
        SupplierDTO supplier = supplierDAO.find(supID);
        if (supplier != null) {
            return ResponseEntity.ok(supplier);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Fixing the method for adding supplier
    @PostMapping("/")
    @Transactional
    public ResponseEntity<SupplierDTO> addSupplier(@RequestBody SupplierDTO supplier) {
        try {
            System.out.println("Request received");

            // Set supplier status (this will be handled automatically from the front end)
            supplier.setSupStatus(1);

            // Save the supplier to the database
            supplierDAO.save(supplier);
            Integer supID = supplier.getSupID(); // Get supplier ID after saving

            System.out.println("Supplier saved in database with ID: " + supID);
            return ResponseEntity.status(HttpStatus.CREATED).body(supplier);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{supID}")
    @Transactional
    public ResponseEntity<SupplierDTO> updateSupplier(
            @PathVariable Integer supID,
            @RequestBody SupplierDTO supplier) { // Change to @RequestBody

        try {
            System.out.println("Request received for updating supplier with ID: " + supID);

            // Find existing supplier in the database
            SupplierDTO existingSupplier = supplierDAO.find(supID);
            if (existingSupplier == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if supplier not found
            }

            // Update supplier information
            existingSupplier.setSupName(supplier.getSupName());
            existingSupplier.setSupEmail(supplier.getSupEmail());
            existingSupplier.setSupPhone(supplier.getSupPhone());
            existingSupplier.setSupAddress(supplier.getSupAddress());

            // Update supplier in the database
            supplierDAO.update(existingSupplier);
            System.out.println("Supplier updated in database");

            return ResponseEntity.ok(existingSupplier);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/search")
    public List<SupplierDTO> searchSuppliers(@RequestParam String term) {
        return supplierDAO.searchSuppliers(term); // Thực hiện tìm kiếm trong DAO
    }

    }




