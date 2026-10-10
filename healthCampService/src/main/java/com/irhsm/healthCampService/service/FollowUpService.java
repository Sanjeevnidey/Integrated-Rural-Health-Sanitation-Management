package com.irhsm.healthCampService.service;

import com.irhsm.healthCampService.dto.PendingItemDto;
import com.irhsm.healthCampService.dto.SummaryDto;
import com.irhsm.healthCampService.entity.FollowUp;
import com.irhsm.healthCampService.entity.FollowUpStatus;
import com.irhsm.healthCampService.entity.ScreeningResult;

import com.irhsm.healthCampService.repository.*;                    // All 5 repositories
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final HealthCampRepository campRepository;
    private final CampBeneficiaryRepository beneficiaryRepository;
    private final ScreeningRepository screeningRepository;

    // Counts for GET /api/camps/summary
    public SummaryDto summary() {
        return new SummaryDto(
                campRepository.count(),                                              // all camps
                followUpRepository.countByStatus(FollowUpStatus.PENDING),            // open followps
                campRepository.countByCampDateAfter(LocalDate.now().minusDays(1)),   // camps today or later
                beneficiaryRepository.count(),                                       // all registrations
                screeningRepository.countByResultNot(ScreeningResult.NORMAL)         // abnormal + critical
        );
    }

    // List for GET /api/camps/pending- PENDING followups, earliest due date first
    public List<PendingItemDto> pending() {
        return followUpRepository.findByStatusOrderByDueDateAsc(FollowUpStatus.PENDING)  
                .stream()                                                                // Go through each one
                .map(this::toPendingItem)                                                // Convert to the team JSON shape
                .toList();                                                               // Collect into a list
    }

    // All follow-ups, for the follow-ups web page
    public List<FollowUp> all() {
        return followUpRepository.findAllByOrderByDueDateAsc();
    }

    // Used by PUT /api/camps/{id}/status when the officer acts on an item
    public PendingItemDto updateStatus(Long id, FollowUpStatus status, String outcome) {
        FollowUp followUp = followUpRepository.findById(id).orElseThrow();  // 404 if the id doesn't exist
        followUp.setStatus(status);                                         // DONE, REFERRED, MISSED or PENDING
        followUp.setOutcome(outcome);                                      
        followUp.setClosedOn(status == FollowUpStatus.PENDING ? null : LocalDate.now()); // Closing date if closed
        return toPendingItem(followUpRepository.save(followUp));            // Save and return the new state
    }

    // Converts a FollowUp entity into the dashboard's JSON shape
    private PendingItemDto toPendingItem(FollowUp f) {
        String title = f.getScreening().getScreeningType() + " "            // e.g. "BP "
                + f.getScreening().getResult().name().toLowerCase()         // "critical"
                + " (" + f.getScreening().getReading() + ") "             // "(168/104 mmHg) – "
                + f.getResidentName();                                      // "Lakshmi R"
        return new PendingItemDto(
                "HEALTH_CAMP",                  
                f.getId(),                      
                title,                          
                f.getVillage(),                 
                f.getStatus().name(),           
                f.getPriority().name(),         
                f.getDueDate()                  
        );
    }
}
