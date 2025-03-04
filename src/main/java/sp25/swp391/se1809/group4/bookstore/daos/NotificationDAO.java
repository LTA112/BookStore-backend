package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.NotificationDTO;

import java.util.List;

public interface NotificationDAO {

    void save(NotificationDTO notificationDTO);
    NotificationDTO find(Integer notID);
    void delete(Integer notID);
    List<NotificationDTO> findAll();
    List<NotificationDTO> search(String notTitle);
}
