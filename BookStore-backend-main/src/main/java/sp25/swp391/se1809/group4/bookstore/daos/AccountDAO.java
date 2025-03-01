package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;

import java.util.List;

public interface AccountDAO {
    void save(AccountDTO accountDTO);

    AccountDTO findByUsername(String username);

    void deleteByUsername(String username);

    List<AccountDTO> findAll();

    public AccountDTO findDetailByUsernameAndStaff(String username, StaffDTO staff);

    public void addAccount(String account);

    List<AccountDTO> searchAccounts(String username);

    public void registerAccount(String account);

    boolean verifyEmail(String otpCodeRequest);

    boolean verifyAccount(String otpCodeRequest);

    boolean changePassword(String passwordRequest);

    void setPassword(String passwordRequest);

    boolean sendEmail(String sendEmailRequest);
}
