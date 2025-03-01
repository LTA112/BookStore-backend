package sp25.swp391.se1809.group4.bookstore.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "cart")
public class CartDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "cartid")
    private Integer cartID;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "username", referencedColumnName = "username")
    private AccountDTO username;

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "bookid", referencedColumnName = "bookid")
    private BookDTO bookID; // Ensure this points to BookDTO
}
