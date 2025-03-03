package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.PromotionLogDTO;

import java.util.Date;
import java.util.List;

public interface PromotionLogDAO {
    void save(PromotionLogDTO promotionLogDTO);
    List<PromotionLogDTO> findAll();
    List<PromotionLogDTO> findByAction(Integer action);
    List<PromotionLogDTO> findByDateRange(Date startDate, Date endDate);
    
}
