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
            //if account with username equals admin has not in database, system will create admin account
            if (accountDAO.findByUsername("admin") == null) {
                //Add account into database
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("admin");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("BookStore");
                accountDTO.setLastName("Administrator's");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("ty24102004@gmail.com");
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
                log.warn("admin user has been created with default password is admin. Please change password!");
            }
            //if account with username equals seller_staff has not in database, system will create seller_staff account
            if (accountDAO.findByUsername("seller_staff") == null) {
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("seller_staff");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("Staff");
                accountDTO.setLastName("Seller");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("ty24102004@gmail.com");
                accountDTO.setAddress("FPT University, Can Tho Campus");
                accountDTO.setPassword(passwordEncoder.encode("seller_staff"));
                accountDTO.setRole(2);
                accountDTO.setPhone("0123456789");
                accountDTO.setSex(1);
                accountDAO.save(accountDTO);

                //Add staff into database
                StaffDTO staffDTO = new StaffDTO();
                staffDTO.setUsername(accountDTO);
                staffDAO.addStaff(staffDTO);
                log.warn("seller staff user has been created with default username and password are seller_staff. Please change password!");
            }
            //if account with username equals warehouse_staff has not in database, system will create warehouse_staff account
            if (accountDAO.findByUsername("warehouse_staff") == null) {
                //Add account into database
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("warehouse_staff");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("Staff");
                accountDTO.setLastName("Warehouse");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("ty24102004@gmail.com");
                accountDTO.setAddress("FPT University, Can Tho Campus");
                accountDTO.setPassword(passwordEncoder.encode("warehouse_staff"));
                accountDTO.setRole(3);
                accountDTO.setSex(1);
                accountDTO.setPhone("0123456789");
                accountDAO.save(accountDTO);

                //Add staff into database
                StaffDTO staffDTO = new StaffDTO();
                staffDTO.setUsername(accountDTO);
                staffDAO.addStaff(staffDTO);
                log.warn("Warehouse staff user has been created with default username and password are warehouse_staff. Please change password!");
            }
            //if account with username equals customer has not in database, system will create customer account
            if (accountDAO.findByUsername("customer") == null) {
                AccountDTO accountDTO = new AccountDTO();
                accountDTO.setUsername("customer");
                accountDTO.setAccStatus(1);
                accountDTO.setFirstName("BookStore");
                accountDTO.setLastName("Customer's");
                accountDTO.setDob(new Date(System.currentTimeMillis()));
                accountDTO.setEmail("ty24102004@gmail.com");
                accountDTO.setAddress("FPT University, Can Tho Campus");
                accountDTO.setPassword(passwordEncoder.encode("customer"));
                accountDTO.setRole(1);
                accountDTO.setPhone("0987654321");
                accountDTO.setSex(1);
                accountDAO.save(accountDTO);
                log.warn("Customer user has been created with default username and password are customer. Please change password!");
            }

        };
    }
}
