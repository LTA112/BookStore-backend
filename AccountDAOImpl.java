package sp25.swp391.se1809.group4.bookstore.daos;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;
import sp25.swp391.se1809.group4.bookstore.request.*;
import sp25.swp391.se1809.group4.bookstore.utils.EmailSenderUtil;
import sp25.swp391.se1809.group4.bookstore.utils.RandomNumberGenerator;

import java.sql.Date;
import java.time.Year;
import java.util.*;

@Repository
public class AccountDAOImpl implements AccountDAO {
    private static final int ACTIVE = 1, INACTIVE = 0, UNVERIFIED = 2, UNVERIFIED_ADMIN = 4;
    private static final String DEFAULT_PASSWORD = "12345";

    private final EntityManager entityManager;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(10);

    @Autowired
    private EmailSenderUtil emailSender;

    @Autowired
    public AccountDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(AccountDTO account) {
        if (findByUsername(account.getUsername()) != null) {
            entityManager.merge(account);
        } else {
            entityManager.persist(account);
        }
    }

    @Override
    public List<AccountDTO> searchAccounts(String key) {
        String jpql = "FROM AccountDTO WHERE LOWER(username) LIKE :key OR LOWER(firstName) LIKE :key OR LOWER(lastName) LIKE :key";
        return entityManager.createQuery(jpql, AccountDTO.class)
                .setParameter("key", "%" + key.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public AccountDTO findByUsername(String username) {
        try {
            Query query = entityManager.createQuery("SELECT a FROM AccountDTO a WHERE a.username = :username", AccountDTO.class);
            query.setParameter("username", username);
            return (AccountDTO) query.getSingleResult();
        } catch (Exception e) {
            System.err.println("Account not found: " + username);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        AccountDTO account = findByUsername(username);
        if (account != null) {
            account.setAccStatus(INACTIVE);
            entityManager.merge(account);
        }
    }

    @Override
    public List<AccountDTO> findAll() {
        return entityManager.createQuery("FROM AccountDTO WHERE accStatus > 0", AccountDTO.class).getResultList();
    }

    @Override
    public AccountDTO findDetailByUsernameAndStaff(String username, StaffDTO staff) {
        AccountDTO account = findByUsername(username);
        if (account != null) {
            account.setStaffDTOCollection(Collections.singletonList(staff));
        }
        return account;
    }

    @Override
    @Transactional
    public void addAccount(String accountStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AccountDTO account = mapper.readValue(accountStr, AccountDTO.class);
            account.setAccStatus(UNVERIFIED_ADMIN);
            account.setPassword(encoder.encode(DEFAULT_PASSWORD));
            entityManager.persist(account);
            if (account.getRole() != 1) {
                StaffDTO staff = new StaffDTO();
                staff.setUsername(account);
                entityManager.persist(staff);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void registerAccount(String accountStr) {
        try {
            AccountDTO account = new ObjectMapper().readValue(accountStr, AccountDTO.class);
            account.setAccStatus(UNVERIFIED);
            account.setRole(1);
            account.setPassword(encoder.encode(account.getPassword()));
            entityManager.persist(account);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public boolean verifyEmail(String otpStr) {
        try {
            VerifyEmailRequest req = new ObjectMapper().readValue(otpStr, VerifyEmailRequest.class);
            AccountDTO account = findByUsername(req.getUsername());
            return account != null && req.getCode().equals(account.getCode());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    @Override
    @Transactional
    public boolean verifyAccount(String otpStr) {
        try {
            VerifyEmailRequest req = new ObjectMapper().readValue(otpStr, VerifyEmailRequest.class);
            AccountDTO account = findByUsername(req.getUsername());
            if (account != null && req.getCode().equals(account.getCode())) {
                account.setAccStatus(ACTIVE);
                entityManager.merge(account);
                return true;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    @Override
    @Transactional
    public boolean changePassword(String passwordStr) {
        try {
            ChangePasswordRequest req = new ObjectMapper().readValue(passwordStr, ChangePasswordRequest.class);
            AccountDTO account = findByUsername(req.getUsername());
            if (account != null && encoder.matches(req.getCurrentPassword(), account.getPassword())) {
                account.setPassword(encoder.encode(req.getNewPassword()));
                entityManager.merge(account);
                return true;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    @Override
    @Transactional
    public void setPassword(String passwordStr) {
        try {
            ResetPasswordRequest req = new ObjectMapper().readValue(passwordStr, ResetPasswordRequest.class);
            AccountDTO account = findByUsername(req.getUsername());
            if (account != null) {
                account.setPassword(encoder.encode(req.getPassword()));
                account.setAccStatus(ACTIVE);
                entityManager.merge(account);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean sendEmail(String emailStr) {
        try {
            SendEmailRequest req = new ObjectMapper().readValue(emailStr, SendEmailRequest.class);
            AccountDTO account = findByUsername(req.getUsername());
            if (account != null && account.getEmail().equals(req.getEmail())) {
                String code = new RandomNumberGenerator().generateNumber();
                account.setCode(code);
                entityManager.merge(account);

                String body = String.format("""
                        <h1 style='text-align: center; background-color: skyblue; color: white;'>BOOK STORE</h1>
                        <p>Dear %s %s,</p>
                        <p>Your verification code is: <strong>%s</strong></p>
                        <p>Do not share this code. For support, contact <strong>ty24102004@gmail.com</strong></p>
                        <p>Best regards,</p>
                        <h3>BookStore</h3>
                        <div style='background-color: darkblue; text-align: center;color: white;'>
                            <em>&copy; %d BookStore. All rights reserved.</em>
                        </div>
                    """,
                        account.getFirstName(), account.getLastName(), code, Year.now().getValue()
                );

                emailSender.sendEmail(req.getEmail(), "VERIFY ACCOUNT OF BOOK STORE", body);
                return true;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
}