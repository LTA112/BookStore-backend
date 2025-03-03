package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.SupplierDTO;

import java.util.List;

public interface SupplierDAO {
    void save(SupplierDTO supplierDTO);
    SupplierDTO find(int supID);
    void update(SupplierDTO supplierDTO);
    void delete(int supID);
        List<SupplierDTO> findAll();
    List<SupplierDTO> searchSuppliers(String searchTerm);

}
