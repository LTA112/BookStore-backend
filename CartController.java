package sp25.swp391.se1809.group4.bookstore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> addBookToCart(@RequestParam String username,
                                                @RequestParam int bookID,
                                                @RequestParam int quantity) {
        try {
            cartDAO.addBookToCart(username, bookID, quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body("Book added to cart successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{username}")
    @Transactional
    public ResponseEntity<List<CartDTO>> viewCart(@PathVariable String username) {
        try {
            List<CartDTO> cart = cartDAO.viewCart(username);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> editQuantity(@RequestParam String username,
                                               @RequestParam int bookID,
                                               @RequestParam int quantity) {
        if (quantity < 1) {
            return ResponseEntity.badRequest().body("Error: Quantity must be at least 1.");
        }
        try {
            cartDAO.editQuantity(username, bookID, quantity);
            return ResponseEntity.ok("Quantity updated successfully for bookID: " + bookID);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteBookFromCart(@RequestParam String username,
                                                     @RequestParam int cartID) {
        try {
            cartDAO.deleteBookFromCart(username, cartID);
            return ResponseEntity.ok("Book removed from cart successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
