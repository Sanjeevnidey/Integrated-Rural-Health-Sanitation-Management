package com.irhsm.healthCampService.controller;

import com.irhsm.healthCampService.dto.PendingItemDto;
import com.irhsm.healthCampService.dto.StatusUpdateRequest;
import com.irhsm.healthCampService.dto.SummaryDto;
import com.irhsm.healthCampService.entity.FollowUpStatus;
import com.irhsm.healthCampService.service.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;          
import org.springframework.web.bind.annotation.*;  

import java.util.List;
import java.util.NoSuchElementException;

@RestController                         
@RequestMapping("/api/camps")           
@RequiredArgsConstructor                
public class DashboardApiController {

    private final FollowUpService followUpService;

    @GetMapping("/summary")             // GET /api/camps/summary
    public SummaryDto summary() {
        return followUpService.summary();           // Spring converts the record to JSON automatically
    }

    @GetMapping("/pending")             // GET /api/camps/pending
    public List<PendingItemDto> pending() {
        return followUpService.pending();           // JSON array of pending items
    }

    @PutMapping("/{id}/status")         // PUT /api/camps/5/status with a JSON body
    public ResponseEntity<?> updateStatus(@PathVariable Long id,                 // {id} from the URL
                                          @RequestBody StatusUpdateRequest req) { // JSON body to Java object
        try {
            FollowUpStatus status = FollowUpStatus.valueOf(req.status().toUpperCase()); 
            return ResponseEntity.ok(followUpService.updateStatus(id, status, req.outcome())); 
        } catch (IllegalArgumentException | NullPointerException e) {           // Unknown or missing status
            return ResponseEntity.badRequest().body("Status must be PENDING, DONE, REFERRED or MISSED");
        } catch (NoSuchElementException e) {                                    // No follow-up with that id
            return ResponseEntity.notFound().build();                           // 404
        }
    }
}