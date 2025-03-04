package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.OrderDetailDTO;

import java.util.List;

public interface OrderDetailDAO {
    void save(OrderDetailDTO orderDetailDTO);
    OrderDetailDTO find(int ODID);
    void update(OrderDetailDTO orderDetailDTO);
    void delete(int ODID);
    List<OrderDetailDTO> findAll();
    List<OrderDetailDTO> findByOrderID(int orderID);
}
