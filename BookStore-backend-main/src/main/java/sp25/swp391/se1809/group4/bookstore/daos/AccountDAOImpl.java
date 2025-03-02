package sp25.swp391.se1809.group4.bookstore.daos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;
import sp25.swp391.se1809.group4.bookstore.request.ChangePasswordRequest;
import sp25.swp391.se1809.group4.bookstore.request.ResetPasswordRequest;
import sp25.swp391.se1809.group4.bookstore.request.SendEmailRequest;
import sp25.swp391.se1809.group4.bookstore.request.VerifyEmailRequest;
import sp25.swp391.se1809.group4.bookstore.utils.EmailSenderUtil;
import sp25.swp391.se1809.group4.bookstore.utils.RandomNumberGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class AccountDAOImpl implements AccountDAO {
    final int ACTIVE_STATUS = 1;
    final int INACTIVE_STATUS = 0;
    final int UNVERIFIED_STATUS = 2;
    final int UNVERIFIED_ADMIN_CREATED_STATUS = 4;
    final String DEFAULT_PASSWORD = "12345";
    //define entity manager
    EntityManager entityManager;
    PasswordEncoder password = new BCryptPasswordEncoder(10);
    @Autowired
    EmailSenderUtil sender;

    @Autowired
    public AccountDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    //implements method
    @Override
    @Transactional
    public void save(AccountDTO accountDTO) {
        AccountDTO account = this.findByUsername(accountDTO.getUsername());
        if (account != null) {
            entityManager.merge(accountDTO);
        } else {
            entityManager.persist(accountDTO);
        }
    }

    @Override
    public List<AccountDTO> searchAccounts(String searchKey) {
        String str = "FROM AccountDTO WHERE LOWER(username) LIKE :searchKey OR LOWER(firstName) LIKE :searchKey OR LOWER(lastName) LIKE :searchKey";
        TypedQuery<AccountDTO> query = entityManager.createQuery(str, AccountDTO.class);
        query.setParameter("searchKey", "%" + searchKey.toLowerCase() + "%");
        return query.getResultList();
    }


    @Override
    public AccountDTO findByUsername(String username) {
        try {
            Query query = entityManager.createQuery("Select a.username, a.firstName, a.lastName, a.dob, a.address, a.email, a.role, a.sex, a.phone, a.accStatus, a.password, a.code From AccountDTO a WHERE a.username=:username");
            query.setParameter("username", username);
            Object[] result = (Object[]) query.getSingleResult();
            AccountDTO account = new AccountDTO();
            account.setUsername((String) result[0]);
            account.setFirstName((String) result[1]);
            account.setLastName((String) result[2]);
            account.setDob((Date) result[3]);
            account.setAddress((String) result[4]);
            account.setEmail((String) result[5]);
            account.setRole((Integer) result[6]);
            account.setSex((Integer) result[7]);
            account.setPhone((String) result[8]);
            account.setAccStatus((Integer) result[9]);
            account.setPassword((String) result[10]);
            account.setCode((String) result[11]);
            return account;
        } catch (Exception e) {
            System.out.println("No found account with username = " + username);
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        AccountDTO account = this.findByUsername(username);
        if (account.getRole() == 1) {
            account.setAccStatus(0);
            entityManager.merge(account);
        } else {
            try {
                TypedQuery<StaffDTO> query = entityManager.createQuery(
                        "SELECT s FROM StaffDTO s WHERE s.username.username = :username", StaffDTO.class
                );
                query.setParameter("username", username);
                StaffDTO staff = query.getSingleResult();
                account.setAccStatus(0);
                entityManager.merge(account);
            } catch (Exception e) {
                System.out.println("Error in findStaff: " + e.getMessage());
            }
        }

    }

    @Override
    public List<AccountDTO> findAll() {
        TypedQuery<AccountDTO> query = entityManager.createQuery("From AccountDTO a WHERE a.accStatus>0", AccountDTO.class);
        return query.getResultList();
    }

    @Override
    public AccountDTO findDetailByUsernameAndStaff(String username, StaffDTO staff) {
        Query query = entityManager.createQuery(
                "SELECT a.username, a.firstName, a.lastName, a.dob, a.email, a.phone, " +
                        "a.role, a.address, a.sex " +
                        "FROM AccountDTO a " +
                        "WHERE a.username = :username");

        query.setParameter("username", username);


        Object[] result = (Object[]) query.getSingleResult();

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUsername((String) result[0]);
        accountDTO.setFirstName((String) result[1]);
        accountDTO.setLastName((String) result[2]);
        accountDTO.setDob((Date) result[3]);
        accountDTO.setEmail((String) result[4]);
        accountDTO.setPhone((String) result[5]);
        accountDTO.setRole((Integer) result[6]);
        accountDTO.setAddress((String) result[7]);
        accountDTO.setSex((Integer) result[8]);
        System.out.println("Staff ID: " + staff.getStaffID());
        Collection<StaffDTO> collection = new ArrayList<>();
        collection.add(staff);
        System.out.println(staff.getUsername().getFirstName());
        accountDTO.setStaffDTOCollection(collection);
        return accountDTO;
    }

    @Override
    @Transactional
    public void addAccount(String account) {
        try {
            //Init object mapper
            ObjectMapper obj = new ObjectMapper();
            //Convert String to account object
            AccountDTO accountDTO = obj.readValue(account, AccountDTO.class);
            accountDTO.setAccStatus(UNVERIFIED_ADMIN_CREATED_STATUS);
            accountDTO.setPassword(password.encode(DEFAULT_PASSWORD));
            entityManager.persist(accountDTO);
            if (accountDTO.getRole() != 1) {
                StaffDTO staff = new StaffDTO();
                staff.setUsername(accountDTO);
                entityManager.persist(staff);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void registerAccount(String account) {
        try {
            //Init object mapper
            ObjectMapper obj = new ObjectMapper();
            //Convert String to account object
            AccountDTO accountDTO = obj.readValue(account, AccountDTO.class);
            accountDTO.setAccStatus(UNVERIFIED_STATUS);
            accountDTO.setRole(1);
            accountDTO.setPassword(password.encode(accountDTO.getPassword()));
            entityManager.persist(accountDTO);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public boolean verifyEmail(String otpCodeRequest) {
        try {
            ObjectMapper obj = new ObjectMapper();
            VerifyEmailRequest request = obj.readValue(otpCodeRequest, VerifyEmailRequest.class);
            AccountDTO account = this.findByUsername(request.getUsername());
            return account.getCode().equals(request.getCode());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Transactional
    @Override
    public boolean verifyAccount(String otpCodeRequest) {
        try {
            ObjectMapper obj = new ObjectMapper();
            VerifyEmailRequest request = obj.readValue(otpCodeRequest, VerifyEmailRequest.class);
            System.out.println(otpCodeRequest);
            AccountDTO account = this.findByUsername(request.getUsername());
            if( account.getCode().equals(request.getCode())){
                account.setAccStatus(1);
                entityManager.merge(account);
            };
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Transactional
    @Override
    public boolean changePassword(String passwordRequest) {
        try {
            System.out.println(passwordRequest);
            ObjectMapper obj = new ObjectMapper();
            ChangePasswordRequest request = obj.readValue(passwordRequest, ChangePasswordRequest.class);
            AccountDTO account = this.findByUsername(request.getUsername());
            System.out.println("Current password: " + request.getCurrentPassword());
            System.out.println(password.matches(request.getCurrentPassword(), account.getPassword()));
            if (password.matches(request.getCurrentPassword(), account.getPassword())) {
                account.setPassword(password.encode(request.getNewPassword()));
                System.out.println(request.getNewPassword());
                entityManager.merge(account);
                System.out.println("Success");
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Transactional
    @Override
    public void setPassword(String passwordRequest) {
        try {
            ObjectMapper obj = new ObjectMapper();
            ResetPasswordRequest request = obj.readValue(passwordRequest, ResetPasswordRequest.class);
            AccountDTO accountDTO = this.findByUsername(request.getUsername());
            accountDTO.setAccStatus(1);
            accountDTO.setPassword(password.encode(request.getPassword()));
            entityManager.merge(accountDTO);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Transactional
    @Override
    public boolean sendEmail(String sendEmailRequest) {
        try {
            ObjectMapper obj = new ObjectMapper();
            SendEmailRequest request = obj.readValue(sendEmailRequest, SendEmailRequest.class);
            AccountDTO accountDTO = findByUsername(request.getUsername());
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();
            accountDTO.setCode(randomNumberGenerator.generateNumber());
            entityManager.merge(accountDTO);
            if (accountDTO.getEmail().equals(request.getEmail())) {
                String toEmail = request.getEmail();
                String subject = "VERIFY ACCOUNT OF BOOK STORE";
                String body = "<h1 style=\"text-align: center; background-color: skyblue; color: white;\">BOOK STORE</h1>\n" +
                        "    <p>Dear " + accountDTO.getFirstName() + " " + accountDTO.getLastName() + ",</p>\n" +
                        "    <p>The code is used to verify account is <strong>" + accountDTO.getCode() + "</strong></p>\n" +
                        "    <p>Please don't give this code for anyone. Thanks for using my service! Hoping you have a special experience at\n" +
                        "        BookStore! For more information please contact <strong>ty24102004@gmail.com</strong></p>\n" +
                        "    <p>Best regard,</p>\n" +
                        "    <h3>BookStore</h3>\n" +
                        "    <div style=\"background-color: darkblue; text-align: center;color: white;line-height: 20px;\">\n" +
                        "        <em>© Copyright " + Year.now().getValue() + "</em><br>\n" +
                        "        <em>BookStore</em><br>\n" +
                        "        <em>All right reserved!</em>\n" +
                        "    </div>";

                sender.sendEmail(toEmail, subject, body);
                System.out.println("Succsess");
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
