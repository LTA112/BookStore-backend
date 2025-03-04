package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;

import java.util.List;

public interface StaffDAO {

    void save(AccountDTO accountDTO);
    StaffDTO findByID(int staffID);
    StaffDTO findStaff(String username);
    void update(String staffDTO);
    void delete(int staffID);
    void deleteStaffByUsername(String username);
    List<StaffDTO> findAll();
    void addStaffByString(String staff);
    void addStaff(StaffDTO staffDTO);
    List<StaffDTO> searchStaffs(String keyword);
}
