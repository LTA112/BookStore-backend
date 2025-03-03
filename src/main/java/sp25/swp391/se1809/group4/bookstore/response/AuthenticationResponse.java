package sp25.swp391.se1809.group4.bookstore.response;

import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token;
    boolean authenticate;
    AccountDTO accountDTO;
}
