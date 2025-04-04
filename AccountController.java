package sp25.swp391.se1809.group4.bookstore.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sp25.swp391.se1809.group4.bookstore.daos.AccountDAO;
import sp25.swp391.se1809.group4.bookstore.daos.StaffDAO;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_CUSTOMER = 1;
    private static final int ROLE_SELLER_STAFF = 2;
    private static final int ROLE_WAREHOUSE_STAFF = 3;
    private static final String DEFAULT_PASSWORD = "12345";

    private final AccountDAO accountDAO;
    private final StaffDAO staffDAO;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Autowired
    public AccountController(AccountDAO accountDAO, StaffDAO staffDAO) {
        this.accountDAO = accountDAO;
        this.staffDAO = staffDAO;
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/")
    public List<AccountDTO> getAccounts() {
        return accountDAO.findAll()
                .stream()
                .filter(a -> Optional.ofNullable(a.getAccStatus()).orElse(0) > 0)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/search")
    public List<AccountDTO> searchAccounts(@RequestParam String keyword) {
        return accountDAO.searchAccounts(keyword)
                .stream()
                .filter(a -> Optional.ofNullable(a.getAccStatus()).orElse(0) > 0)
                .collect(Collectors.toList());
    }

    @GetMapping("/{username}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String username) {
        AccountDTO account = accountDAO.findByUsername(username);
        if (account == null || Optional.ofNullable(account.getAccStatus()).orElse(0) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(account);
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("/")
    public ResponseEntity<AccountDTO> addAccount(@RequestPart("account") String account) {
        try {
            accountDAO.addAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error adding account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AccountDTO> registeredAccount(@RequestPart("register") String account) {
        try {
            accountDAO.registerAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error registering account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{username}")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable String username,
            @RequestPart("account") String accountJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AccountDTO accountDTO = objectMapper.readValue(accountJson, AccountDTO.class);

            AccountDTO existingAccount = accountDAO.findByUsername(username);
            if (existingAccount == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            int oldRole = existingAccount.getRole();
            updateAccountDetails(existingAccount, accountDTO);
            accountDAO.save(existingAccount);

            updateStaffRelation(existingAccount, oldRole);

            return ResponseEntity.ok(existingAccount);
        } catch (IOException e) {
            log.error("Error updating account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void updateAccountDetails(AccountDTO existing, AccountDTO update) {
        existing.setUsername(update.getUsername());
        existing.setFirstName(update.getFirstName());
        existing.setLastName(update.getLastName());
        if (update.getRole() != null) existing.setRole(update.getRole());
        existing.setSex(update.getSex());
        existing.setPhone(update.getPhone());
        existing.setEmail(update.getEmail());
        existing.setAddress(update.getAddress());
        existing.setDob(update.getDob());
        existing.setAccStatus(1);
    }

    private void updateStaffRelation(AccountDTO accountDTO, int oldRole) {
        if (accountDTO.getRole() == ROLE_CUSTOMER && oldRole != ROLE_CUSTOMER) {
            StaffDTO staff = staffDAO.findStaff(accountDTO.getUsername());
            if (staff != null) {
                staffDAO.delete(staff.getStaffID());
            }
        } else if (accountDTO.getRole() != ROLE_CUSTOMER && oldRole == ROLE_CUSTOMER) {
            StaffDTO newStaff = new StaffDTO();
            newStaff.setUsername(accountDTO);
            staffDAO.addStaff(newStaff);
        }
    }

    @PutMapping("/change")
    public ResponseEntity<AccountDTO> changePassword(@RequestPart("password-request") String passwordRequest) {
        boolean success = accountDAO.changePassword(passwordRequest);
        return ResponseEntity.status(success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteAccount(@PathVariable String username) {
        AccountDTO account = accountDAO.findByUsername(username);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        if (account.getRole() != ROLE_CUSTOMER) {
            StaffDTO staff = staffDAO.findStaff(username);
            if (staff != null) staffDAO.delete(staff.getStaffID());
        }
        accountDAO.deleteByUsername(username);
        return ResponseEntity.ok("Account deleted successfully!");
    }

    @PostMapping("/email/send/")
    public ResponseEntity<Boolean> sendEmail(@RequestPart("forgot-password") String sendEmailRequest) {
        accountDAO.sendEmail(sendEmailRequest);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/password/reset/")
    public ResponseEntity<Boolean> resetPassword(@RequestPart("password") String passwordRequest) {
        accountDAO.setPassword(passwordRequest);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/email/verify/")
    public ResponseEntity<Boolean> verifyEmail(@RequestPart("code") String otpCodeRequest) {
        accountDAO.verifyEmail(otpCodeRequest);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/account/verify/")
    public ResponseEntity<Boolean> verifyAccount(@RequestPart("code") String otpCodeRequest) {
        accountDAO.verifyAccount(otpCodeRequest);
        return ResponseEntity.ok(true);
    }
}
