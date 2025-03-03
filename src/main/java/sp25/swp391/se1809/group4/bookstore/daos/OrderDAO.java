package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.OrderDTO;

import java.util.List;

public interface OrderDAO {
    void save(OrderDTO orderDTO);
    OrderDTO find(int orderID);
    void update(OrderDTO orderDTO);
    void delete(int orderID);
    List<OrderDTO> findAll();
    List<OrderDTO> searchByOrderId(int orderID);
}
