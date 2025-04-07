package sp25.swp391.se1809.group4.bookstore.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sp25.swp391.se1809.group4.bookstore.daos.*;
import sp25.swp391.se1809.group4.bookstore.models.*;

import java.text.SimpleDateFormat;
import java.util.*;

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
        this.accountDAO = accountDAO;
        this.promotionLogDAO = promotionLogDAO;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllPromotions() {
        try {
            return ResponseEntity.ok(buildPromotionResponseList(promotionDAO.findAll()));
        } catch (Exception e) {
            return handleException("An error occurred while fetching promotions.", e);
        }
    }

    @GetMapping("/{proID}")
    public ResponseEntity<?> getPromotionById(@PathVariable int proID) {
        try {
            PromotionDTO promotion = promotionDAO.find(proID);
            if (promotion == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Promotion not found");
            return ResponseEntity.ok(buildPromotionResponse(promotion));
        } catch (Exception e) {
            return handleException("An error occurred", e);
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> addPromotion(@RequestBody PromotionDTO promotionDTO, @RequestParam String username) {
        try {
            StaffDTO staff = staffDAO.findStaff(username);
            if (staff == null) return badRequest("Invalid username: Staff not found.");
            if (promotionDTO.getProName() == null || promotionDTO.getProCode() == null)
                return badRequest("Promotion name and code are required.");

            promotionDTO.setCreatedBy(staff);
            promotionDTO.setProStatus(1);
            promotionDAO.save(promotionDTO);

            createLog(promotionDTO, 1, staff);
            return ResponseEntity.status(HttpStatus.CREATED).body(promotionDTO);
        } catch (Exception e) {
            return handleException("Error occurred while saving promotion.", e);
        }
    }

    @PutMapping("/{proID}")
    @Transactional
    public ResponseEntity<?> updatePromotion(@PathVariable int proID, @RequestBody Map<String, Object> updates) {
        try {
            PromotionDTO promotion = promotionDAO.find(proID);
            if (promotion == null) return notFound("Promotion with ID " + proID + " not found.");

            updatePromotionFields(promotion, updates);

            if (updates.containsKey("actionId")) {
                Integer actionId = (Integer) updates.get("actionId");
                if (actionId != 2 && actionId != 3) return badRequest("Invalid actionId. Must be 2 (approve) or 3 (reject).");
                if (!updates.containsKey("staffID")) return badRequest("Missing staffID in request.");

                StaffDTO staff = new StaffDTO();
                staff.setStaffID((Integer) updates.get("staffID"));
                if (actionId == 2) promotion.setApprovedBy(staff);
                else promotion.setProStatus(0);

                createLog(promotion, actionId, staff);
            }

            promotionDAO.update(promotion);
            return ResponseEntity.ok("Promotion updated successfully.");
        } catch (Exception e) {
            return handleException("An error occurred while updating the promotion.", e);
        }
    }

    @DeleteMapping("/{proID}")
    @Transactional
    public ResponseEntity<String> deletePromotion(@PathVariable int proID, @RequestParam int staffID) {
        try {
            PromotionDTO promotion = promotionDAO.find(proID);
            if (promotion == null) return notFound("Promotion not found.");
            if (promotion.getProStatus() == 0) return badRequest("Promotion is already marked as deleted.");

            promotion.setProStatus(0);
            promotionDAO.update(promotion);

            logPromotionAction(4, promotion, staffID);
            return ResponseEntity.ok("Promotion deleted successfully.");
        } catch (Exception e) {
            return handleException("An error occurred while deleting the promotion.", e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<PromotionDTO>> searchPromotions(@RequestParam(required = false) Integer id,
                                                               @RequestParam(required = false) String term) {
        if (id != null) {
            PromotionDTO promotion = promotionDAO.find(id);
            return ResponseEntity.ok(promotion != null ? List.of(promotion) : List.of());
        }
        return ResponseEntity.ok(term != null && !term.isEmpty() ? promotionDAO.searchPromotions(term) : List.of());
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getAllPromotionLogs(@RequestParam(required = false) Integer action,
                                                 @RequestParam(required = false) String activity,
                                                 @RequestParam(required = false) String startDate) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Logs API not implemented in this snippet.");
    }

    private void createLog(PromotionDTO promotion, int actionId, StaffDTO staff) {
        PromotionLogDTO log = new PromotionLogDTO();
        log.setProId(promotion);
        log.setProAction(actionId);
        log.setProLogDate(new Date());
        log.setStaffId(staff);
        promotionLogDAO.save(log);
    }

    private void updatePromotionFields(PromotionDTO promotion, Map<String, Object> updates) throws Exception {
        if (updates.containsKey("quantity")) promotion.setQuantity((Integer) updates.get("quantity"));
        if (updates.containsKey("startDate")) promotion.setStartDate(parseDate((String) updates.get("startDate")));
        if (updates.containsKey("endDate")) promotion.setEndDate(parseDate((String) updates.get("endDate")));
        if (updates.containsKey("proStatus")) promotion.setProStatus((Integer) updates.get("proStatus"));
    }

    private Date parseDate(String date) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }

    private List<Map<String, Object>> buildPromotionResponseList(List<PromotionDTO> promotions) {
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (PromotionDTO promotion : promotions) responseList.add(buildPromotionResponse(promotion));
        return responseList;
    }

    private Map<String, Object> buildPromotionResponse(PromotionDTO promotion) {
        Map<String, Object> map = new HashMap<>();
        map.put("proID", promotion.getProID());
        map.put("proName", promotion.getProName());
        map.put("proCode", promotion.getProCode());
        map.put("discount", promotion.getDiscount());
        map.put("startDate", promotion.getStartDate());
        map.put("endDate", promotion.getEndDate());
        map.put("quantity", promotion.getQuantity());
        map.put("proStatus", promotion.getProStatus());
        map.put("createdBy", promotion.getCreatedBy() != null ? promotion.getCreatedBy().getStaffID() : null);
        map.put("approvedBy", promotion.getApprovedBy() != null ? promotion.getApprovedBy().getStaffID() : null);
        return map;
    }

    private void logPromotionAction(int actionId, PromotionDTO promotion, int staffID) {
        StaffDTO staff = new StaffDTO();
        staff.setStaffID(staffID);
        createLog(promotion, actionId, staff);
    }

    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    private ResponseEntity<String> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    private ResponseEntity<String> handleException(String message, Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}