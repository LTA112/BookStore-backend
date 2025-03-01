package sp25.swp391.se1809.group4.bookstore.controllers;

import sp25.swp391.se1809.group4.bookstore.daos.AccountDAO;
import sp25.swp391.se1809.group4.bookstore.daos.PromotionDAO;
import sp25.swp391.se1809.group4.bookstore.daos.PromotionLogDAO;
import sp25.swp391.se1809.group4.bookstore.daos.StaffDAO;
import fa24.swp391.se1802.group3.capybook.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sp25.swp391.se1809.group4.bookstore.models.PromotionDTO;
import sp25.swp391.se1809.group4.bookstore.models.PromotionLogDTO;
import sp25.swp391.se1809.group4.bookstore.models.StaffDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionDAO promotionDAO;
    private final StaffDAO staffDAO;
    private final AccountDAO accountDAO;
    private final PromotionLogDAO promotionLogDAO;

    @Autowired
    public PromotionController(PromotionDAO promotionDAO, StaffDAO staffDAO, AccountDAO accountDAO, PromotionLogDAO promotionLogDAO) {
        this.promotionDAO = promotionDAO;
        this.staffDAO = staffDAO;
        this.accountDAO = accountDAO; // Inject AccountDAO
        this.promotionLogDAO = promotionLogDAO;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllPromotions() {
        try {
            // Fetch all promotions từ DAO
            List<PromotionDTO> promotions = promotionDAO.findAll();

            // Map promotions sang response DTO
            List<Map<String, Object>> responseList = promotions.stream().map(promotion -> {
                Map<String, Object> response = new HashMap<>();
                response.put("proID", promotion.getProID());
                response.put("proName", promotion.getProName());
                response.put("proCode", promotion.getProCode());
                response.put("discount", promotion.getDiscount());
                response.put("startDate", promotion.getStartDate());
                response.put("endDate", promotion.getEndDate());
                response.put("quantity", promotion.getQuantity());
                response.put("proStatus", promotion.getProStatus());

                // Fetch staff ID hoặc null
                response.put("createdBy", promotion.getCreatedBy() != null
                        ? promotion.getCreatedBy().getStaffID()
                        : null);
                response.put("approvedBy", promotion.getApprovedBy() != null
                        ? promotion.getApprovedBy().getStaffID()
                        : null);
                return response;
            }).toList();

            // Return the response
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching promotions.");
        }
    }

    @GetMapping("/{proID}")
    public ResponseEntity<?> getPromotionById(@PathVariable int proID) {
        try {
            PromotionDTO promotion = promotionDAO.find(proID);
            if (promotion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Promotion not found");
            }

            // Tạo phản hồi JSON chỉ chứa các trường cần thiết
            Map<String, Object> response = new HashMap<>();
            response.put("proID", promotion.getProID());
            response.put("proName", promotion.getProName());
            response.put("proCode", promotion.getProCode());
            response.put("discount", promotion.getDiscount());
            response.put("startDate", promotion.getStartDate());
            response.put("endDate", promotion.getEndDate());
            response.put("quantity", promotion.getQuantity());
            response.put("proStatus", promotion.getProStatus());

            // Fetch staff ID hoặc null
            response.put("createdBy", promotion.getCreatedBy() != null
                    ? promotion.getCreatedBy().getStaffID()
                    : null);
            response.put("approvedBy", promotion.getApprovedBy() != null
                    ? promotion.getApprovedBy().getStaffID()
                    : null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred");
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> addPromotion(
            @RequestBody PromotionDTO promotionDTO,
            @RequestParam(name = "username") String username) {
        try {
            // Kiểm tra username và lấy thông tin Staff
            StaffDTO staff = staffDAO.findStaff(username);
            if (staff == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid username: Staff not found.");
            }

            // Kiểm tra dữ liệu Promotion
            if (promotionDTO.getProName() == null || promotionDTO.getProCode() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Promotion name and code are required.");
            }

            // Gắn Staff vào Promotion
            promotionDTO.setCreatedBy(staff);
            promotionDTO.setProStatus(1); // Mặc định là Active

            // Lưu promotion vào cơ sở dữ liệu
            promotionDAO.save(promotionDTO);

            // Tạo log cho hành động "Create"
            PromotionLogDTO promotionLog = new PromotionLogDTO();
            promotionLog.setProId(promotionDTO);
            promotionLog.setProAction(1); // Action ID cho "Create"
            promotionLog.setProLogDate(new Date());
            promotionLog.setStaffId(staff);
            promotionLogDAO.save(promotionLog); // Lưu log vào cơ sở dữ liệu

            return ResponseEntity.status(HttpStatus.CREATED).body(promotionDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while saving promotion.");
        }
    }

    @PutMapping("/{proID}")
    @Transactional
    public ResponseEntity<?> updatePromotion(
            @PathVariable int proID,
            @RequestBody Map<String, Object> updates) {
        try {
            System.out.println("Received request to update promotion with ID: " + proID);
            System.out.println("Updates: " + updates);

            // Kiểm tra tồn tại của promotion
            PromotionDTO existingPromotion = promotionDAO.find(proID);
            if (existingPromotion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Promotion with ID " + proID + " not found.");
            }

            // Kiểm tra và cập nhật các trường
            if (updates.containsKey("quantity")) {
                Integer quantity = (Integer) updates.get("quantity");
                existingPromotion.setQuantity(quantity);
            }

            if (updates.containsKey("startDate")) {
                String startDateString = (String) updates.get("startDate");
                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
                existingPromotion.setStartDate(startDate);
            }

            if (updates.containsKey("endDate")) {
                String endDateString = (String) updates.get("endDate");
                Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDateString);
                existingPromotion.setEndDate(endDate);
            }

            if (updates.containsKey("proStatus")) {
                Integer proStatus = (Integer) updates.get("proStatus");
                existingPromotion.setProStatus(proStatus);
            }

            // Ghi log nếu có actionId
            if (updates.containsKey("actionId")) {
                Integer actionId = (Integer) updates.get("actionId");

                if (actionId == 2 || actionId == 3) { // Approved hoặc Rejected
                    if (!updates.containsKey("staffID")) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Missing staffID in request.");
                    }

                    Integer staffID = (Integer) updates.get("staffID");
                    StaffDTO staff = new StaffDTO();
                    staff.setStaffID(staffID);

                    // Nếu actionId là approve
                    if (actionId == 2) {
                        existingPromotion.setApprovedBy(staff);
                    } else if (actionId == 3) {
                        existingPromotion.setProStatus(0); // Đánh dấu là đã từ chối
                    }

                    // Ghi log
                    PromotionLogDTO promotionLog = new PromotionLogDTO();
                    promotionLog.setProId(existingPromotion);
                    promotionLog.setProAction(actionId);
                    promotionLog.setProLogDate(new Date());
                    promotionLog.setStaffId(staff);
                    promotionLogDAO.save(promotionLog);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid actionId. Must be 2 (approve) or 3 (reject).");
                }
            }

            // Cập nhật promotion
            promotionDAO.update(existingPromotion);
            return ResponseEntity.ok("Promotion updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the promotion.");
        }
    }

    @DeleteMapping("/{proID}")
    @Transactional
    public ResponseEntity<String> deletePromotion(
            @PathVariable int proID,
            @RequestParam(name = "staffID") int staffID) {
        try {
            System.out.println("Received DELETE request:");
            System.out.println("Promotion ID: " + proID);
            System.out.println("Staff ID: " + staffID);

            // Tìm promotion theo ID
            PromotionDTO existingPromotion = promotionDAO.find(proID);
            if (existingPromotion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Promotion not found.");
            }

            // Kiểm tra trạng thái hiện tại
            if (existingPromotion.getProStatus() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Promotion is already marked as deleted.");
            }

            // Cập nhật trạng thái thành 0
            existingPromotion.setProStatus(0);
            promotionDAO.update(existingPromotion);

            // Ghi log hành động "Delete"
            logPromotionAction(4, existingPromotion, staffID); // Action ID = 4 cho "Delete"

            return ResponseEntity.ok("Promotion deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the promotion.");
        }
    }

    // Phương thức ghi log hành động
    private void logPromotionAction(int actionId, PromotionDTO promotion, int staffID) {
        PromotionLogDTO promotionLog = new PromotionLogDTO();
        promotionLog.setProId(promotion);
        promotionLog.setProAction(actionId); // ID hành động (1 = Create, 2 = Approve, 3 = Reject, 4 = Delete)
        promotionLog.setProLogDate(new Date());

        StaffDTO staff = new StaffDTO();
        staff.setStaffID(staffID);
        promotionLog.setStaffId(staff);

        promotionLogDAO.save(promotionLog);
    }


    @GetMapping("/search")
    public ResponseEntity<List<PromotionDTO>> searchPromotions(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String term) {
        if (id != null) {
            PromotionDTO promotion = promotionDAO.find(id);
            if (promotion != null) {
                return ResponseEntity.ok(List.of(promotion));
            } else {
                return ResponseEntity.ok(List.of());
            }
        }

        if (term != null && !term.isEmpty()) {
            List<PromotionDTO> promotions = promotionDAO.searchPromotions(term);
            return ResponseEntity.ok(promotions);
        }

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getAllPromotionLogs(
            @RequestParam(required = false) Integer action,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            List<PromotionLogDTO> logs;

            if (activity != null) {
                // Mapping activity to action
                Integer actionId = switch (activity.toLowerCase()) {
                    case "create" -> 1;
                    case "approve" -> 2;
                    case "reject" -> 3;
                    case "delete" -> 4; // Thêm action delete
                    default -> null;
                };

                if (actionId == null) {
                    return ResponseEntity.badRequest().body("Invalid activity type.");
                }

                logs = promotionLogDAO.findByAction(actionId);
            } else if (startDate != null && endDate != null) {
                Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
                Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
                logs = promotionLogDAO.findByDateRange(start, end);
            } else {
                logs = promotionLogDAO.findAll();
            }

            // Convert logs to response
            List<Map<String, Object>> responseLogs = logs.stream().map(log -> {
                Map<String, Object> response = new HashMap<>();
                response.put("proLogId", log.getProLogId());
                response.put("proId", log.getProId().getProID());
                response.put("proAction", log.getProAction());
                response.put("proLogDate", log.getProLogDate());
                response.put("staffId", log.getStaffId().getStaffID());
                return response;
            }).toList();

            return ResponseEntity.ok(responseLogs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching promotion logs.");
        }
    }

}
