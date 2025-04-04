package sp25.swp391.se1809.group4.bookstore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sp25.swp391.se1809.group4.bookstore.daos.CartDAO;
import sp25.swp391.se1809.group4.bookstore.models.CartDTO;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartDAO cartDAO;

    @Autowired
    public CartController(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam("username") String user,
                                            @RequestParam("bookID") int bookId,
                                            @RequestParam("quantity") int qty) {
        try {
            cartDAO.addBookToCart(user, bookId, qty);
            return new ResponseEntity<>("Book successfully added to cart.", HttpStatus.CREATED);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add book: " + ex.getMessage());
        }
    }



    @PutMapping("/update")
    public ResponseEntity<String> updateItemQuantity(@RequestParam("username") String user,
                                                     @RequestParam("bookID") int bookId,
                                                     @RequestParam("quantity") int qty) {
        if (qty <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be greater than zero.");
        }
        try {
            cartDAO.editQuantity(user, bookId, qty);
            return ResponseEntity.ok("Updated quantity for book ID: " + bookId);
        } catch (RuntimeException re) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found: " + re.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }


}
