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
        // Tìm trong giỏ hàng với username và bookID
        String jpql = "FROM CartDTO WHERE username.username = :username AND bookID.bookID = :bookID";
        TypedQuery<CartDTO> query = entityManager.createQuery(jpql, CartDTO.class);
        query.setParameter("username", username);
        query.setParameter("bookID", bookID);

        CartDTO cartItem;

        if (!query.getResultList().isEmpty()) {
            // Nếu đã có trong giỏ, cập nhật số lượng
            cartItem = query.getSingleResult();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            entityManager.merge(cartItem);
        } else {
            // Nếu chưa có, thêm mới
            cartItem = new CartDTO();
            cartItem.setUsername(entityManager.find(AccountDTO.class, username));
            cartItem.setBookID(entityManager.find(BookDTO.class, bookID));
            cartItem.setQuantity(quantity);
            entityManager.persist(cartItem);
        }
    }

    @Override
    public List<CartDTO> viewCart(String username) {
        String jpql = "SELECT c FROM CartDTO c WHERE c.username.username = :username";
        TypedQuery<CartDTO> query = entityManager.createQuery(jpql, CartDTO.class);
        query.setParameter("username", username);
        List<CartDTO> cartList = query.getResultList();

        // In ra console để kiểm tra


        return cartList;
    }


    @Override
    @Transactional
    public void editQuantity(String username, int cartID, int quantity) {
        String jpql = "FROM CartDTO WHERE username.username = :username AND cartID = :cartID";
        TypedQuery<CartDTO> query = entityManager.createQuery(jpql, CartDTO.class);
        query.setParameter("username", username);
        query.setParameter("cartID", cartID);

        if (!query.getResultList().isEmpty()) {
            CartDTO cartItem = query.getSingleResult();
            cartItem.setQuantity(quantity);
            entityManager.merge(cartItem);
        } else {
            throw new RuntimeException("Cart item not found with ID: " + cartID);
        }
    }


    @Override
    @Transactional
    public void deleteBookFromCart(String username, int cartID) {
        // Logic xóa (sử dụng JPQL hoặc Native Query như trên)
        String jpql = "DELETE FROM CartDTO c WHERE c.id = :cartID AND c.username.username = :username";
        int deletedCount = entityManager.createQuery(jpql)
                .setParameter("cartID", cartID)
                .setParameter("username", username)
                .executeUpdate();
        if (deletedCount > 0) {
            System.out.println("Cart item deleted successfully with cartID: " + cartID);
        } else {
            throw new RuntimeException("Cart item not found or username does not match.");
        }
    }
}









