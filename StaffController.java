package sp25.swp391.se1809.group4.bookstore.controllers;

import sp25.swp391.se1809.group4.bookstore.daos.AccountDAO;
import sp25.swp391.se1809.group4.bookstore.daos.StaffDAO;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;
import sp25.swp391.se1809.group4.bookstore.response.StaffResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/staffs")
public class StaffController {

    private final StaffDAO staffDAO;
    private final AccountDAO accountDAO;

    @Autowired
    public StaffController(StaffDAO staffDAO, AccountDAO accountDAO) {
        this.staffDAO = staffDAO;
        this.accountDAO = accountDAO;
    }

    @GetMapping("/")
    public ResponseEntity<List<StaffDTO>> getAllStaff() {
        List<StaffDTO> staffList = staffDAO.findAll();
        return staffList.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(staffList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable String id) {
        return Optional.ofNullable(staffDAO.findByID(Integer.parseInt(id)))
                .map(staff -> {
                    AccountDTO account = staff.getUsername();
                    StaffResponse response = new StaffResponse();
                    response.setStaffID(staff.getStaffID());
                    response.setUsername(account.getUsername());
                    response.setFirstName(account.getFirstName());
                    response.setLastName(account.getLastName());
                    response.setEmail(account.getEmail());
                    response.setPhone(account.getPhone());
                    response.setDob(account.getDob());
                    response.setSex(account.getSex());
                    response.setRole(account.getRole());
                    response.setAddress(account.getAddress());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<StaffDTO> getStaff(@PathVariable String username) {
        return Optional.ofNullable(accountDAO.findByUsername(username))
                .map(account -> {
                    StaffDTO staffDTO = staffDAO.findStaff(username);
                    return ResponseEntity.ok(staffDTO);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/")
    public ResponseEntity<StaffResponse> addStaff(@RequestPart("staff") String staff) {
        try {
            staffDAO.addStaffByString(staff);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/")
    public ResponseEntity<StaffResponse> updateStaff(@RequestPart("staff") String staff) {
        try {
            staffDAO.update(staff);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<StaffDTO>> searchAccounts(@RequestParam String keyword) {
        List<StaffDTO> result = staffDAO.searchStaffs(keyword);
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @DeleteMapping("/{staffID}")
    public ResponseEntity<String> deleteStaff(@PathVariable String staffID) {
        try {
            staffDAO.delete(Integer.parseInt(staffID));
            return ResponseEntity.ok("Account deleted successfully!");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid staff ID format.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete staff.");
        }
    }
}
