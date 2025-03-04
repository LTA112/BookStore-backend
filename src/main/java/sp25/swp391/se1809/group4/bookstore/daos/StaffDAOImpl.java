package sp25.swp391.se1809.group4.bookstore.daos;

import com.fasterxml.jackson.databind.ObjectMapper;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;
import sp25.swp391.se1809.group4.bookstore.request.StaffRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class StaffDAOImpl implements  StaffDAO{
    EntityManager entityManager;
    static final String DEFAULT_PASSWORD = "12345";
    final int UNVERIFIED_STATUS = 4;

    @Autowired
    public StaffDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    AccountDAO accountDAO;

    @Transactional
    @Override
    public void save(AccountDTO accountDTO) {
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setUsername(accountDTO);
        staffDTO.setStaffID(0);
        entityManager.persist(staffDTO);
    }

    @Override
    public StaffDTO findByID(int staffID) {
        StaffDTO staff = entityManager.find(StaffDTO.class, staffID);
        return staff;
    }

    @Override
    public StaffDTO findStaff(String username) {
        try {
            TypedQuery<StaffDTO> query = entityManager.createQuery(
                    "SELECT s FROM StaffDTO s WHERE s.username.username = :username", StaffDTO.class
            );
            query.setParameter("username", username); // Truyền trực tiếp chuỗi username
            return query.getSingleResult();
        } catch (Exception e) {
            System.out.println("Error in findStaff: " + e.getMessage());
            return null;
        }
    }


    @Override
    @Transactional
    public void update(String staff) {
        try {
            System.out.println("Staff: "+ staff);
            ObjectMapper objectMapper = new ObjectMapper();
            StaffRequest staffDTO = objectMapper.readValue(staff,StaffRequest.class);

            StaffDTO existingStaff = findByID(staffDTO.getStaffID());
            if(existingStaff!=null){
                //handle account of staff
                AccountDTO account = new AccountDTO();
                account.setDob(staffDTO.getDob());
                account.setAddress(staffDTO.getAddress());
                account.setSex(staffDTO.getSex());
                account.setFirstName(staffDTO.getFirstName());
                account.setPhone(staffDTO.getPhone());
                account.setRole(staffDTO.getRole());
                account.setLastName(staffDTO.getLastName());
                account.setEmail(staffDTO.getEmail());
                account.setPassword(existingStaff.getUsername().getPassword());
                account.setUsername(existingStaff.getUsername().getUsername());
                existingStaff.setUsername(account);
                account.setAccStatus(1);

                //update staff and account
                entityManager.merge(account);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Transactional
    @Override
    public void delete(int staffID) {
        //Find staff
        StaffDTO staff = this.findByID(staffID);
        //Find account
        AccountDTO account = entityManager.find(AccountDTO.class,staff.getUsername().getUsername());
        //Set status account
        account.setAccStatus(0);
        //Update status of account
        entityManager.merge(account);
    }

    @Transactional
    @Override
    public void deleteStaffByUsername(String username) {
        StaffDTO staff = this.findStaff(username);
        entityManager.remove(staff);
    }

    @Override
    public List<StaffDTO> findAll() {
        TypedQuery<StaffDTO> query = entityManager.createQuery("From StaffDTO s WHERE s.username.accStatus>0", StaffDTO.class);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void addStaffByString(String staff) {
        try {
            ObjectMapper obj = new ObjectMapper();
            StaffRequest request = obj.readValue(staff,StaffRequest.class);
            AccountDTO account = new AccountDTO();
            account.setUsername(request.getUsername());
            account.setDob(request.getDob());
            account.setAddress(request.getAddress());
            account.setSex(request.getSex());
            account.setEmail(request.getEmail());
            account.setPhone(request.getPhone());
            account.setRole(request.getRole());
            account.setLastName(request.getLastName());
            account.setFirstName(request.getFirstName());
            account.setAccStatus(UNVERIFIED_STATUS);
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            account.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

            StaffDTO staffDTO = new StaffDTO();
            staffDTO.setUsername(account);

            entityManager.persist(account);
            entityManager.flush();
            entityManager.persist(staffDTO);
            entityManager.flush();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    @Override
    @Transactional
    public void addStaff(StaffDTO staff) {
            entityManager.persist(staff);
            entityManager.flush();
    }

    @Override
    public List<StaffDTO> searchStaffs(String keyword) {
        List<StaffDTO> list = new ArrayList<>();
        String str = "FROM StaffDTO s WHERE CAST(s.staffID AS string) LIKE :keyword OR LOWER(s.username.firstName) LIKE :keyword OR LOWER(s.username.lastName) LIKE :keyword";
        TypedQuery<StaffDTO> query = entityManager.createQuery(str, StaffDTO.class);
        query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        for (StaffDTO staffDTO : query.getResultList()) {
            if(staffDTO.getUsername().getAccStatus()!= null && staffDTO.getUsername().getAccStatus()>0){
                list.add(staffDTO);
            }
        }
        return list;
    }
}
