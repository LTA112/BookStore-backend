package sp25.swp391.se1809.group4.bookstore.controllers;

import sp25.swp391.se1809.group4.bookstore.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")

public class PaymentController {

    private final VnPayService vnPayService;

    @Autowired
    public PaymentController(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    /**
     * Tạo URL thanh toán VNPay
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestParam long totalAmount, HttpServletRequest request) {
        try {
            // Xác định URL trả về sau khi thanh toán
            String returnUrl = "http://localhost:5173/PaymentSuccessPage";
            // Gọi service để tạo URL thanh toán
            String paymentUrl = vnPayService.createOrder(totalAmount, "Thanh toán đơn hàng", returnUrl, request);

            return ResponseEntity.status(HttpStatus.OK).body(paymentUrl); // Trả về URL thanh toán
        } catch (Exception e) {
            System.err.println("Error while creating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> handlePaymentReturn(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Xác minh giao dịch
            int paymentStatus = vnPayService.orderReturn(request);

            String transactionId = request.getParameter("vnp_TransactionNo");
            String amount = request.getParameter("vnp_Amount");
            String formattedAmount = String.valueOf(Long.parseLong(amount) / 100);

            if (paymentStatus == 1) {
                response.put("status", "success");
                response.put("message", "Payment successful!");
                response.put("transactionId", transactionId);
                response.put("amount", formattedAmount);
            } else {
                response.put("status", "error");
                response.put("message", "Payment failed. Please try again.");
                response.put("transactionId", transactionId);
                response.put("amount", formattedAmount);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred while processing the payment.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

