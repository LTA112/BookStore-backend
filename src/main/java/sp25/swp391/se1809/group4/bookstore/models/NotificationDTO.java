package sp25.swp391.se1809.group4.bookstore.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification")
public class NotificationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Basic(optional = false)
    @Column(name = "notid")
    private Integer notID;

    @Column(name = "nottitle")
    private String notTitle;

    @Column(name = "receiver")
    private Integer receiver;

    @Lob
    @Column(name = "notdescription")
    private String notDescription;

    @Column(name = "notstatus")
    private Integer notStatus;

    @JoinColumn(name = "staffid", referencedColumnName = "staffid")
    @ManyToOne
    @JsonBackReference
    private StaffDTO staffID;
}
