package sp25.swp391.se1809.group4.bookstore.controllers;


import sp25.swp391.se1809.group4.bookstore.daos.*;
import sp25.swp391.se1809.group4.bookstore.models.*;
import sp25.swp391.se1809.group4.bookstore.utils.EmailSenderUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;


@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderDAO orderDAO;
    private final OrderDetailDAO orderDetailDAO;
    private final BookDAO bookDAO;
    private final PromotionDAO promotionDAO;
    private final AccountDAO accountDAO;
    private final EmailSenderUtil emailSenderUtil;
    StringBuilder emailContent = new StringBuilder();


    @Autowired
    public OrderController(OrderDAO orderDAO, OrderDetailDAO orderDetailDAO, BookDAO bookDAO, PromotionDAO promotionDAO, AccountDAO accountDAO,EmailSenderUtil emailSenderUtil) {
        this.orderDAO = orderDAO;
        this.orderDetailDAO = orderDetailDAO;
        this.bookDAO = bookDAO;
        this.promotionDAO = promotionDAO;
        this.accountDAO = accountDAO;
        this.emailSenderUtil = emailSenderUtil;
    }
    @GetMapping("/")
    public List<OrderDTO> getAllOrders() {
        List<OrderDTO> orders = orderDAO.findAll();
        for (OrderDTO order : orders) {
            if (order.getUsername() != null) {
                order.setCustomerName(order.getUsername().getUsername()); // Sử dụng username trực tiếp
            }
        }
        return orders;
    }

    @GetMapping("/{orderID}")
    @Transactional
    public ResponseEntity<Map<String, Object>> getOrderWithDetails(@PathVariable int orderID) {
        OrderDTO order = orderDAO.find(orderID);
        List<OrderDetailDTO> orderDetails = orderDetailDAO.findByOrderID(orderID);

        if (order != null) {
            Map<String, Object> response = new HashMap<>();

            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderID", order.getOrderID());
            orderMap.put("orderDate", order.getOrderDate());
            orderMap.put("orderStatus", order.getOrderStatus());
            orderMap.put("username", order.getUsername().getUsername());
            orderMap.put("orderAddress", order.getOrderAddress());

            // Thêm staffID vào phản hồi
            orderMap.put("processedBy", order.getStaffID() != null ? order.getStaffID().getStaffID() : null);

            if (order.getProID() != null) {
                orderMap.put("proID", order.getProID().getProID());
                orderMap.put("discount", order.getProID().getDiscount());
            }

            response.put("order", orderMap);

            List<Map<String, Object>> orderDetailResponses = orderDetails.stream()
                    .map(detail -> {
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("ODID", detail.getOdid());
                        detailMap.put("quantity", detail.getQuantity());
                        detailMap.put("bookID", detail.getBookID().getBookID());
                        detailMap.put("totalPrice", detail.getTotalPrice());
                        return detailMap;
                    })
                    .collect(Collectors.toList());

            response.put("orderDetails", orderDetailResponses);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/")
    @Transactional
    public ResponseEntity<String> addOrder(@RequestBody Map<String, Object> orderData) {
        System.out.println("Request received: " + orderData); // Log dữ liệu nhận được
        try {
            emailContent.setLength(0); // Reset nội dung emailContent
            // Lấy thông tin order và chi tiết order từ request body
            Map<String, Object> orderDTOMap = (Map<String, Object>) orderData.get("orderDTO");
            List<Map<String, Object>> orderDetailsList = (List<Map<String, Object>>) orderData.get("orderDetails");

            // Kiểm tra dữ liệu đầu vào
            if (orderDTOMap == null || orderDetailsList == null || orderDetailsList.isEmpty()) {
                return new ResponseEntity<>("Invalid order data: Missing required fields.", HttpStatus.BAD_REQUEST);
            }

            // Tạo đối tượng OrderDTO
            OrderDTO orderDTO = new OrderDTO();
            String username = (String) orderDTOMap.get("username");
            AccountDTO account = accountDAO.findByUsername(username);
            if (account == null) {
                return new ResponseEntity<>("Invalid username: User does not exist.", HttpStatus.BAD_REQUEST);
            }
            orderDTO.setUsername(account);

            // Lấy địa chỉ và email từ orderDTOMap
            String orderAddress = (String) orderDTOMap.get("orderAddress");
            String email = (String) orderDTOMap.get("email");

            if (orderAddress == null || orderAddress.trim().isEmpty()) {
                return new ResponseEntity<>("Order address is required.", HttpStatus.BAD_REQUEST);
            }
            if (email == null || email.trim().isEmpty()) {
                return new ResponseEntity<>("Email is required.", HttpStatus.BAD_REQUEST);
            }

            orderDTO.setOrderAddress(orderAddress);

            // Xử lý khuyến mãi (nếu có)
            Integer proID = (Integer) orderDTOMap.get("proID");
            if (proID != null) {
                PromotionDTO promotion = promotionDAO.find(proID);
                if (promotion == null || promotion.getQuantity() <= 0) {
                    return new ResponseEntity<>("Invalid promotion: Not available or out of stock.", HttpStatus.BAD_REQUEST);
                }
                promotion.setQuantity(promotion.getQuantity() - 1);
                promotionDAO.update(promotion);
                orderDTO.setProID(promotion);
            }

            // Đặt ngày và trạng thái đơn hàng
            orderDTO.setOrderDate(new Date());
            orderDTO.setOrderStatus(0);

            // Lưu OrderDTO
            orderDAO.save(orderDTO);

            for (Map<String, Object> detail : orderDetailsList) {
                Integer bookID = (Integer) detail.get("bookID");
                Integer quantity = (Integer) detail.get("quantity");
                BigDecimal totalPrice = new BigDecimal(detail.get("totalPrice").toString());

                BookDTO book = bookDAO.find(bookID);
                if (book == null) {
                    return new ResponseEntity<>("Invalid book ID: Book not found.", HttpStatus.BAD_REQUEST);
                }

                // Kiểm tra số lượng sách
                if (book.getBookQuantity() < quantity) {
                    return new ResponseEntity<>(
                            "Insufficient stock for book ID: " + bookID,
                            HttpStatus.BAD_REQUEST
                    );
                }

                // Trừ số lượng sách trong kho
                book.setBookQuantity(book.getBookQuantity() - quantity);
                bookDAO.update(book);

                // Tạo OrderDetail
                OrderDetailDTO orderDetail = new OrderDetailDTO();
                orderDetail.setOrderID(orderDTO);
                orderDetail.setBookID(book);
                orderDetail.setQuantity(quantity);
                orderDetail.setTotalPrice(totalPrice);
                orderDetailDAO.save(orderDetail);
            }

            // Bắt đầu xây dựng nội dung email
            emailContent.append("<html><body>");
            emailContent.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ccc; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>");

// Tiêu đề chính
            emailContent.append("<div style='background-color: #2e6c80; color: #fff; padding: 16px; text-align: center; border-radius: 8px 8px 0 0;'>");
            emailContent.append("<h1 style='margin: 0;'>Capybook Store</h1>");
            emailContent.append("</div>");

// Thông báo thành công
            emailContent.append("<div style='padding: 16px; background-color: #f9f9f9;'>");
            emailContent.append("<h2 style='color: #2e6c80; text-align: center;'>You have successfully placed your order!</h2>");
            emailContent.append("</div>");

// Thông tin khách hàng
            emailContent.append("<div style='padding: 16px;'>");
            emailContent.append("<h3 style='color: #333;'>Customer Information</h3>");
            emailContent.append("<p><strong>Name:</strong> ").append(account.getFirstName()).append(" ").append(account.getLastName()).append("</p>");
            emailContent.append("<p><strong>Phone Number:</strong> ").append(account.getPhone()).append("</p>");
            emailContent.append("<p><strong>Address:</strong> ").append(orderDTO.getOrderAddress()).append("</p>");
            emailContent.append("</div>");

// Bảng thông tin sách trong đơn hàng
            emailContent.append("<div style='padding: 16px;'>");
            emailContent.append("<h3 style='color: #333;'>Books in Your Order</h3>");
            emailContent.append("<table border='1' style='border-collapse: collapse; width: 100%; font-family: Arial, sans-serif;'>");

// Tiêu đề bảng
            emailContent.append("<thead>");
            emailContent.append("<tr style='background-color: #e6f7ff; color: #333;'>");
            emailContent.append("<th style='padding: 8px; text-align: left;'>#</th>");
            emailContent.append("<th style='padding: 8px; text-align: left;'>Book Title</th>");
            emailContent.append("<th style='padding: 8px; text-align: center;'>Quantity</th>");
            emailContent.append("<th style='padding: 8px; text-align: right;'>Unit Price</th>");
            emailContent.append("<th style='padding: 8px; text-align: right;'>Total Price</th>");
            emailContent.append("</tr>");
            emailContent.append("</thead>");

// Nội dung bảng
            emailContent.append("<tbody>");
            int index = 1;
            BigDecimal totalPrice = BigDecimal.ZERO;

            for (Map<String, Object> detail : orderDetailsList) {
                Integer bookID = (Integer) detail.get("bookID");
                Integer quantity = (Integer) detail.get("quantity");
                BigDecimal unitPrice = new BigDecimal(detail.get("totalPrice").toString()).divide(BigDecimal.valueOf(quantity));
                BigDecimal itemTotalPrice = new BigDecimal(detail.get("totalPrice").toString());
                BookDTO book = bookDAO.find(bookID);

                totalPrice = totalPrice.add(itemTotalPrice);

                emailContent.append("<tr>");
                emailContent.append("<td style='padding: 8px;'>").append(index++).append("</td>");
                emailContent.append("<td style='padding: 8px;'>").append(book.getBookTitle()).append("</td>");
                emailContent.append("<td style='padding: 8px; text-align: center;'>").append(quantity).append("</td>");
                emailContent.append("<td style='padding: 8px; text-align: right;'>").append(formatCurrency(unitPrice)).append("</td>");
                emailContent.append("<td style='padding: 8px; text-align: right;'>").append(formatCurrency(itemTotalPrice)).append("</td>");
                emailContent.append("</tr>");
            }

// Tổng giá trị đơn hàng
            emailContent.append("<tr style='background-color: #f2f2f2;'>");
            emailContent.append("<td colspan='4' style='padding: 8px; text-align: right; font-weight: bold;'>Total Books Price:</td>");
            emailContent.append("<td style='padding: 8px; text-align: right; font-weight: bold;'>").append(formatCurrency(totalPrice)).append("</td>");
            emailContent.append("</tr>");

            BigDecimal finalPrice = totalPrice; // Giá trị cuối cùng
            BigDecimal discountPercent = BigDecimal.ZERO; // Giá trị mặc định của discount
            BigDecimal discountAmount = BigDecimal.ZERO; // Giá trị mặc định của discount amount

            if (orderDTO.getProID() != null && orderDTO.getProID().getDiscount() > 0) {
                // Nếu có Promotion, tính Discount
                discountPercent = BigDecimal.valueOf(orderDTO.getProID().getDiscount());
                discountAmount = totalPrice.multiply(discountPercent).divide(BigDecimal.valueOf(100));
                finalPrice = totalPrice.subtract(discountAmount);
            }

// Hiển thị Discount
            emailContent.append("<tr style='background-color: #f2f2f2;'>");
            emailContent.append("<td colspan='4' style='padding: 8px; text-align: right; font-weight: bold;'>Discount (")
                    .append(discountPercent).append("%):</td>");
            emailContent.append("<td style='padding: 8px; text-align: right; font-weight: bold;'>-").append(formatCurrency(discountAmount)).append("</td>");
            emailContent.append("</tr>");

// Hiển thị tổng giá trị cuối cùng
            emailContent.append("<tr style='background-color: #e6f7ff;'>");
            emailContent.append("<td colspan='4' style='padding: 8px; text-align: right; font-weight: bold;'>Final Total Price:</td>");
            emailContent.append("<td style='padding: 8px; text-align: right; font-weight: bold;'>").append(formatCurrency(finalPrice)).append("</td>");
            emailContent.append("</tr>");
            emailContent.append("</tbody>");
            emailContent.append("</table>");
            emailContent.append("</div>");


// Lời cảm ơn
            emailContent.append("<div style='padding: 16px; text-align: center;'>");
            emailContent.append("<p>Thank you for choosing Capybook!</p>");
            emailContent.append("<p>We are committed to delivering your order as quickly as possible.</p>");
            emailContent.append("<p style='font-weight: bold;'>Best regards,<br/>The Capybook Team</p>");
            emailContent.append("</div>");

// Kết thúc khung
            emailContent.append("</div>");
            emailContent.append("</body></html>");

// Gửi email
            String subject = "Order Confirmation - Capybook";
            String body = emailContent.toString();

            emailSenderUtil.sendEmail(email, subject, body);

            return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create order: " + e.getMessage());
        }
    }

    @PutMapping("/{orderID}")
    @Transactional
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable int orderID, @RequestBody Map<String, Object> updateData) {
        OrderDTO existingOrder = orderDAO.find(orderID);

        if (existingOrder == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            Integer newStatus = (Integer) updateData.get("orderStatus");
            Integer staffID = (Integer) updateData.get("staffID");

            if (newStatus == null || staffID == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Lấy danh sách OrderDetail theo OrderID
            List<OrderDetailDTO> orderDetails = orderDetailDAO.findByOrderID(orderID);

            // Logic tăng số lượng sách khi chuyển từ "Proccessing" sang "Cancle"
            if (existingOrder.getOrderStatus() == 0 && newStatus == 1) {
                for (OrderDetailDTO detail : orderDetails) {
                    BookDTO book = detail.getBookID();
                    book.setBookQuantity(book.getBookQuantity() + detail.getQuantity());
                    bookDAO.update(book);
                }
            }
            
            // Logic tăng số lượng sách khi chuyển từ "Delivering" sang "Returned"
            if (existingOrder.getOrderStatus() == 2 && newStatus == 4) {
                for (OrderDetailDTO detail : orderDetails) {
                    BookDTO book = detail.getBookID();
                    book.setBookQuantity(book.getBookQuantity() + detail.getQuantity());
                    bookDAO.update(book);
                }
            }

            // Gắn trực tiếp staffID vào đơn hàng
            StaffDTO staff = new StaffDTO();
            staff.setStaffID(staffID);
            existingOrder.setStaffID(staff);

            // Cập nhật trạng thái đơn hàng
            existingOrder.setOrderStatus(newStatus);
            orderDAO.update(existingOrder);

            return new ResponseEntity<>(existingOrder, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Xóa đơn hàng
    @DeleteMapping("/{orderID}")
    public ResponseEntity<String> deleteOrder(@PathVariable int orderID) {
        OrderDTO existingOrder = orderDAO.find(orderID);
        if (existingOrder != null) {
            orderDAO.delete(orderID);
            return new ResponseEntity<>("Order deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<OrderDTO>> searchOrders(@RequestParam("term") int orderID) {
        List<OrderDTO> orders = orderDAO.searchByOrderId(orderID);
        for (OrderDTO order : orders) {
            if (order.getUsername() != null) {
                order.setCustomerName(order.getUsername().getFirstName() + " " + order.getUsername().getLastName());
            }
        }
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
    // Phương thức định dạng giá trị
    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " VND";
    }

}
