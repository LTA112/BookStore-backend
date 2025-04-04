package sp25.swp391.se1809.group4.bookstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import static org.junit.jupiter.api.Assertions.*;

import sp25.swp391.se1809.group4.bookstore.controllers.AccountController;
import sp25.swp391.se1809.group4.bookstore.models.AccountDTO;

@SpringBootTest
class BookStoreApplicationTests {

    @Autowired
    private AccountController accountController;

    @Test
    void contextLoads() {
        assertNotNull(accountController);
    }

    @Test
    void testRegisterAccount() throws JsonProcessingException {
        // Tạo đối tượng AccountDTO
        AccountDTO account = new AccountDTO();
        account.setEmail("test@example.com");
        account.setPassword("password123");
        account.setFirstName("John");
        account.setLastName("Doe");

        // Chuyển đổi account thành JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String accountJson = objectMapper.writeValueAsString(account);

        // Gửi request với dữ liệu dưới dạng request part
        MultiValueMap<String, String> requestPart = new LinkedMultiValueMap<>();
        requestPart.add("register", accountJson);

        ResponseEntity<?> response = accountController.registeredAccount(accountJson);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
    }
}