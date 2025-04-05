package sp25.swp391.se1809.group4.bookstore.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import sp25.swp391.se1809.group4.bookstore.daos.AccountDAO;
import sp25.swp391.se1809.group4.bookstore.daos.StaffDAO;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j // cho phép sử dụng log.infor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    final int ROLE_ADMIN = 0;
    final int ROLE_CUSTOMER = 1;
    final int ROLE_SELLER_STAFF = 2;
    final int ROLE_WAREHOUSE_STAFF = 3;
    final String DEFAULT_PASSWORD = "12345";
    AccountDAO accountDAO;
    StaffDAO staffDAO;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Autowired
    public AccountController(AccountDAO accountDAO, StaffDAO staffDAO) {
        this.accountDAO = accountDAO;
        this.staffDAO = staffDAO;
    }


    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/")
    public List<AccountDTO> getAccounts() {
        List<AccountDTO> list = new ArrayList<>();
        for (AccountDTO accountDTO : accountDAO.findAll()) {
            if (accountDTO.getAccStatus() != null && accountDTO.getAccStatus() > 0) {
                list.add(accountDTO);
            }
        }
        return list;
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/search")
    public List<AccountDTO> searchAccounts(@RequestParam String keyword) {
        List<AccountDTO> list = new ArrayList<>();
        for (AccountDTO accountDTO : accountDAO.searchAccounts(keyword)) {
            if (accountDTO.getAccStatus() != null && accountDTO.getAccStatus() > 0) {
                list.add(accountDTO);
            }
        }
        return list;
    }

    @GetMapping("/{username}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String username) {
        AccountDTO account = accountDAO.findByUsername(username);
        System.out.println(account.getAccStatus());
        if (account.getAccStatus() != null && account.getAccStatus() != 0) {
            return ResponseEntity.ok(account);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping(value = "/")
    public ResponseEntity<AccountDTO> addAccount(@RequestPart("account") String account) {
        try {
            accountDAO.addAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/register")
    public ResponseEntity<AccountDTO> registeredAccount(@RequestPart("register") String account) {
        try {
            accountDAO.registerAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{username}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable String username, @RequestPart("account") String account) {
        try {
            int oldRole=0;
            ObjectMapper objectMapper = new ObjectMapper();
            AccountDTO accountDTO = objectMapper.readValue(account, AccountDTO.class);

            AccountDTO existingAccount = accountDAO.findByUsername(username);
            if (existingAccount == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            oldRole = existingAccount.getRole();
            existingAccount.setUsername(accountDTO.getUsername());
            existingAccount.setFirstName(accountDTO.getFirstName());
            existingAccount.setLastName(accountDTO.getLastName());
            if (accountDTO.getRole() != null) {
                existingAccount.setRole(accountDTO.getRole());
            }
            existingAccount.setSex(accountDTO.getSex());
            existingAccount.setPhone(accountDTO.getPhone());
            existingAccount.setEmail(accountDTO.getEmail());
            existingAccount.setAddress(accountDTO.getAddress());
            existingAccount.setDob(accountDTO.getDob());
            existingAccount.setAccStatus(1);
            accountDAO.save(existingAccount);

            if(existingAccount.getRole()==1 && oldRole!=1){
                staffDAO.delete(staffDAO.findStaff(username).getStaffID());
            }
            if(existingAccount.getRole()!=1 && oldRole==1){
                StaffDTO staff = new StaffDTO();
                staff.setUsername(accountDTO);
                staffDAO.addStaff(staff);
            }

            return ResponseEntity.ok(existingAccount);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/change")
    public ResponseEntity<AccountDTO> changePassword(@RequestPart("password-request") String passwordRequest) {

        if (accountDAO.changePassword(passwordRequest)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteAccount(@PathVariable String username) {
        AccountDTO account = accountDAO.findByUsername(username);
        if (account != null) {
            if (account.getRole() != 1) {
                staffDAO.delete(staffDAO.findStaff(username).getStaffID());
            } else {
                accountDAO.deleteByUsername(username);
            }
            return ResponseEntity.ok("Account deleted successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }
    }

    @PostMapping("/email/send/")
    public ResponseEntity<Boolean> sendEmail(@RequestPart("forgot-password") String sendEmailRequest) {
        accountDAO.sendEmail(sendEmailRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/password/reset/")
    public ResponseEntity<Boolean> resetPassword(@RequestPart("password") String passwordRequest) {
        accountDAO.setPassword(passwordRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/email/verify/")
    public ResponseEntity<Boolean> verifyEmail(@RequestPart("code") String otpCodeRequest) {
        accountDAO.verifyEmail(otpCodeRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/account/verify/")
    public ResponseEntity<Boolean> verifyAccount(@RequestPart("code") String otpCodeRequest) {
        accountDAO.verifyAccount(otpCodeRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
