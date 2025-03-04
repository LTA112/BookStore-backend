package sp25.swp391.se1809.group4.bookstore.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "category")
public class CategoryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "catid")
    private Integer catID;

    @Column(name = "catname", nullable = false)
    private String catName;

    @Column(name = "catstatus", nullable = false)
    private Integer catStatus;

    @Lob
    @Column(name = "catdescription")
    private String catDescription;

    @OneToMany(mappedBy = "catId")
    @JsonIgnore // Ngăn Jackson tuần tự hóa danh sách này
    private List<BookCategoryDTO> bookCategoryList;

}

