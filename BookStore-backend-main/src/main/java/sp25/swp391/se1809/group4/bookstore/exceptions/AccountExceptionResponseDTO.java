package sp25.swp391.se1809.group4.bookstore.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountExceptionResponseDTO {
    private int status;
    private String message;
    private long timeStamp;
}
