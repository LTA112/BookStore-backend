package sp25.swp391.se1809.group4.bookstore.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "importstockdetail")
public class ImportStockDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "isdid")
    private Integer isdid;

    @Column(name = "isdquantity")
    @JsonProperty("iSDQuantity")
    private Integer iSDQuantity;

    @Column(name = "importprice")
    private BigDecimal importPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookid", referencedColumnName = "bookid")
    private BookDTO bookID;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isid", referencedColumnName = "isid")
    @JsonIgnore // Bỏ qua trong JSON để tránh vòng lặp
    private ImportStockDTO isid;

}
