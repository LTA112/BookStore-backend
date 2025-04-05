package sp25.swp391.se1809.group4.bookstore.daos;

import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import sp25.swp391.se1809.group4.bookstore.models.BookDTO;
import sp25.swp391.se1809.group4.bookstore.models.CartDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CartDAOImpl implements CartDAO {

    private final EntityManager entityManager;

    @Autowired
    public CartDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void addBookToCart(String username, int bookID, int quantity) {
        CartDTO cartItem = null;

        List<CartDTO> result = entityManager.createQuery(
                        "SELECT c FROM CartDTO c WHERE c.username.username = :username AND c.bookID.bookID = :bookID",
                        CartDTO.class
                )
                .setParameter("username", username)
                .setParameter("bookID", bookID)
                .getResultList();

        if (!result.isEmpty()) {
            cartItem = result.get(0);
            int currentQty = cartItem.getQuantity();
            cartItem.setQuantity(currentQty + quantity);
            entityManager.merge(cartItem);
        } else {
            AccountDTO account = entityManager.find(AccountDTO.class, username);
            BookDTO book = entityManager.find(BookDTO.class, bookID);
            cartItem = new CartDTO();
            cartItem.setUsername(account);
            cartItem.setBookID(book);
            cartItem.setQuantity(quantity);
            entityManager.persist(cartItem);
        }
    }

    @Override
    public List<CartDTO> viewCart(String username) {
        return entityManager.createQuery(
                        "SELECT c FROM CartDTO c WHERE c.username.username = :username",
                        CartDTO.class
                )
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    @Transactional
    public void editQuantity(String username, int cartID, int quantity) {
        List<CartDTO> items = entityManager.createQuery(
                        "SELECT c FROM CartDTO c WHERE c.username.username = :username AND c.cartID = :cartID",
                        CartDTO.class
                )
                .setParameter("username", username)
                .setParameter("cartID", cartID)
                .getResultList();

        if (!items.isEmpty()) {
            CartDTO cartItem = items.get(0);
            cartItem.setQuantity(quantity);
            entityManager.merge(cartItem);
        } else {
            throw new IllegalArgumentException("Không tìm thấy giỏ hàng có ID: " + cartID);
        }
    }

    @Override
    @Transactional
    public void deleteBookFromCart(String username, int cartID) {
        int affectedRows = entityManager.createQuery(
                        "DELETE FROM CartDTO c WHERE c.id = :cartID AND c.username.username = :username"
                )
                .setParameter("cartID", cartID)
                .setParameter("username", username)
                .executeUpdate();

        if (affectedRows == 0) {
            throw new IllegalStateException("Không thể xóa vì không tìm thấy cartID phù hợp.");
        }
    }
}
