package sp25.swp391.se1809.group4.bookstore.configs;

import sp25.swp391.se1809.group4.bookstore.daos.AccountDAO;
import sp25.swp391.se1809.group4.bookstore.daos.StaffDAO;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;


@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Configuration
public class ApplicationInitConfig {

    @Autowired
    StaffDAO staffDAO;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    /**
     *  Application Runner method: The method will check account in database. If database haven't any account yet.
     *  The system will create default admin account
     * @param accountDAO data access object of account entity
     * @return Application Runner
     */
    @Bean
    ApplicationRunner applicationRunner(AccountDAO accountDAO) {
        return args -> {
            //if account with username equals Admin has not in database, system will create Admin account
            if (accountDAO.findByUsername("Admin") == null) {
                //Add account into database
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("Admin");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("BookStore");
                accountDTO.setLastName("Administrator's");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("bookstoreteam@gmail.com");
                accountDTO.setAddress("FPT University, Can Tho Campus");
                accountDTO.setPassword(passwordEncoder.encode("admin"));
                accountDTO.setRole(0);
                accountDTO.setPhone("0987654321");
                accountDTO.setSex(1);
                accountDAO.save(accountDTO);

                //Add staff into database
                StaffDTO staffDTO = new StaffDTO();
                staffDTO.setUsername(accountDTO);
                staffDAO.addStaff(staffDTO);
                log.warn("Admin user has been created with default password is admin. Please change password!");
            }
            //if account with username equals Staff has not in database, system will create Staff account
            if (accountDAO.findByUsername("Staff") == null) {
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("Staff");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("Staff");
                accountDTO.setLastName("");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("bookstoreteam@gmail.com");
                accountDTO.setAddress("FPT University, Can Tho Campus");
                accountDTO.setPassword(passwordEncoder.encode("staff"));
                accountDTO.setRole(2);
                accountDTO.setPhone("0123456789");
                accountDTO.setSex(1);
                accountDAO.save(accountDTO);

                //Add staff into database
                StaffDTO staffDTO = new StaffDTO();
                staffDTO.setUsername(accountDTO);
                staffDAO.addStaff(staffDTO);
                log.warn("Staff user has been created with default username and password are staff. Please change password!");
            }
            //if account with username equals tHHH has not in database, system will create tHHH account
            if (accountDAO.findByUsername("tHHH") == null) {
                //Add account into database
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("tHHH");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("HHH");
                accountDTO.setLastName("NNN");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("thuongnthca180249@fpt.edu.vn");
                accountDTO.setAddress("FPT University, Can Tho Campus");
                accountDTO.setPassword(passwordEncoder.encode("tHHH"));
                accountDTO.setRole(2);
                accountDTO.setSex(1);
                accountDTO.setPhone("0123456789");
                accountDAO.save(accountDTO);

                //Add staff into database
                StaffDTO staffDTO = new StaffDTO();
                staffDTO.setUsername(accountDTO);
                staffDAO.addStaff(staffDTO);
                log.warn("tHHH user has been created with default username and password are tHHH. Please change password!");
            }
            //if account with username equals M.Trinh has not in database, system will create M.Trinh account
            if (accountDAO.findByUsername("M.Trinh") == null) {
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("M.Trinh");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("Trinh");
                accountDTO.setLastName("Mai");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("trinh@gmail.com");
                accountDTO.setAddress("FPT University, Can Tho Campus");
                accountDTO.setPassword(passwordEncoder.encode("m.trinh"));
                accountDTO.setRole(1);
                accountDTO.setPhone("0238635814");
                accountDTO.setSex(1);
                accountDAO.save(accountDTO);
                log.warn("M.Trinh user has been created with default username and password are m.trinh. Please change password!");
            }

        };
    }
}
