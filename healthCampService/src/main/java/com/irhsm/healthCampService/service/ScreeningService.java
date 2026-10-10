package com.irhsm.healthCampService.service;

import com.irhsm.healthCampService.entity.*;                        // Entities and enums
import com.irhsm.healthCampService.repository.CampBeneficiaryRepository;
import com.irhsm.healthCampService.repository.FollowUpRepository;
import com.irhsm.healthCampService.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;                              // Lombok: constructor for the final fields
import org.springframework.stereotype.Service;                      // Marks this class as a service bean
import org.springframework.transaction.annotation.Transactional; 

import java.time.LocalDate;  

@Service 
@RequiredArgsConstructor 

public class ScreeningService {
    private final ScreeningRepository screeningRepository;          // Saves screenings
    private final CampBeneficiaryRepository beneficiaryRepository;  // Loads and updates the beneficiary
    private final FollowUpRepository followUpRepository;             // Saves follow-ups
    
    @Transactional  // Screening + attendance + follow up are saved together or not at all
    public Screening record(Long beneficiaryId, Screening screening) {
        CampBeneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)   // Find who is being screened
                .orElseThrow();                                                         // Throws NoSuchElementException if not found

        screening.setBeneficiary(beneficiary);                    // Link the screening to the beneficiary
        Screening saved = screeningRepository.save(screening);    

        beneficiary.setAttendance(Attendance.ATTENDED);           
        beneficiaryRepository.save(beneficiary);                  // UPDATE camp_beneficiary

        if (saved.getResult() != ScreeningResult.NORMAL) {        // THE RULE: ABNORMAL or CRITICAL automatically creates a follow-up
            createFollowUp(saved, beneficiary);                  
        }
        return saved;                                             
    }

    private void createFollowUp(Screening screening, CampBeneficiary beneficiary) {
        FollowUp followUp = new FollowUp();                       // New empty follow-up
        followUp.setScreening(screening);                         
        followUp.setResidentId(beneficiary.getResidentId());      // Copy resident details so the dashboard
        followUp.setResidentName(beneficiary.getResidentName());  
        followUp.setVillage(beneficiary.getVillage());            // the Resident service

        if (screening.getResult() == ScreeningResult.CRITICAL) {  // Critical- urgent referral
            followUp.setPriority(Priority.HIGH);
            followUp.setAction(FollowUpAction.REFER_TO_CENTRE);
            followUp.setDueDate(LocalDate.now().plusDays(2));     // Due in 2 days
        } else {                                                  // Abnormal- recheck later
            followUp.setPriority(Priority.MEDIUM);
            followUp.setAction(FollowUpAction.RECHECK);
            followUp.setDueDate(LocalDate.now().plusDays(14));    // Due in 14 days
        }
        followUp.setStatus(FollowUpStatus.PENDING);               // Starts as pending
        followUpRepository.save(followUp);                       
    }
}
