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

@RestController
@RequestMapping("/api/v1/staffs")
public class StaffController {
    private final StaffDAO staffDAO;

    @Autowired
    AccountDAO accountDAO;

    @Autowired
    public StaffController(StaffDAO staffDAO) {
        this.staffDAO = staffDAO;
    }

    // Endpoint to fetch a single staff member by ID
    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable String id) {
        StaffDTO staff = staffDAO.findByID(Integer.parseInt(id));
        if (staff != null) {
            StaffResponse staffResponse = new StaffResponse();
            staffResponse.setUsername(staff.getUsername().getUsername());
            staffResponse.setDob(staff.getUsername().getDob());
            staffResponse.setEmail(staff.getUsername().getEmail());
            staffResponse.setRole(staff.getUsername().getRole());
            staffResponse.setAddress(staff.getUsername().getAddress());
            staffResponse.setFirstName(staff.getUsername().getFirstName());
            staffResponse.setLastName(staff.getUsername().getLastName());
            staffResponse.setPhone(staff.getUsername().getPhone());
            staffResponse.setSex(staff.getUsername().getSex());
            staffResponse.setStaffID(staff.getStaffID());
            return ResponseEntity.ok(staffResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // New endpoint to fetch all staff members
    @GetMapping("/")
    public ResponseEntity<List<StaffDTO>> getAllStaff() {
        List<StaffDTO> staffList = staffDAO.findAll();
        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<StaffDTO> getStaff(@PathVariable String username) {
        System.out.println("Input: " + username);
        AccountDTO account = accountDAO.findByUsername(username);
        if (account != null) {
            StaffDTO staffDTO = staffDAO.findStaff(username);
            return ResponseEntity.status(HttpStatus.OK).body(staffDTO);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PutMapping("/")
    public ResponseEntity<StaffResponse> updateStaff(@RequestPart("staff") String staff) {
        staffDAO.update(staff);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/")
    public ResponseEntity<StaffResponse> addStaff(@RequestPart("staff") String staff) {
        staffDAO.addStaffByString(staff);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/search")
    public List<StaffDTO> searchAccounts(@RequestParam String keyword) {
        return staffDAO.searchStaffs(keyword);
    }

    @DeleteMapping("/{staffID}")
    public ResponseEntity<String> deleteStaff(@PathVariable String staffID) {
        staffDAO.delete(Integer.parseInt(staffID));
        return ResponseEntity.ok("Account deleted successfully!");
    }
}
