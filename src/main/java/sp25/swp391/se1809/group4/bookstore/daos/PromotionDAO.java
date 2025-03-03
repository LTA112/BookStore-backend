package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.PromotionDTO;

import java.util.List;

public interface PromotionDAO {
    void save(PromotionDTO promotionDTO);
    PromotionDTO find(int proID);
    void update(PromotionDTO promotionDTO);
    void delete(int proID);
    List<PromotionDTO> findAll();
    List<PromotionDTO> searchPromotions(String searchTerm);
}
