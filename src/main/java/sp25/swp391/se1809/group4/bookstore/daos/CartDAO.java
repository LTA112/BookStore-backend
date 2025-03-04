package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.CartDTO;

import java.util.List;

public interface CartDAO {
    void addBookToCart(String username, int bookID, int quantity);
    List<CartDTO> viewCart(String username);
    void editQuantity(String username, int bookID, int quantity);
    void deleteBookFromCart(String username, int cartID);
}
