package com.irhsm.healthCampService.service;

import com.irhsm.healthCampService.entity.*;
import com.irhsm.healthCampService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampService {

    private final HealthCampRepository campRepository;
    private final CampStaffRepository staffRepository;            
    private final CampBeneficiaryRepository beneficiaryRepository;
    private final ScreeningRepository screeningRepository;        

    // Camps
    public List<HealthCamp> search(String q, String village, CampStatus status) {
        return campRepository.findAll(Sort.by("campDate").descending()).stream()
                .filter(c -> q == null || q.isBlank() || c.getName().toLowerCase().contains(q.toLowerCase()))
                .filter(c -> village == null || village.isBlank() || c.getVillage().equalsIgnoreCase(village))
                .filter(c -> status == null || c.getStatus() == status)
                .toList();
    }

    public List<String> villages() {
        return campRepository.findAll().stream().map(HealthCamp::getVillage).distinct().sorted().toList();
    }

    public HealthCamp get(Long id) {
        return campRepository.findById(id).orElseThrow();
    }

    public HealthCamp save(HealthCamp camp) {
        return campRepository.save(camp);
    }

    //Detail page data
    public List<CampStaff> staff(Long campId) {
        return staffRepository.findByCampId(campId);              // Staff of this camp
    }

    public List<CampBeneficiary> beneficiaries(Long campId) {
        return beneficiaryRepository.findByCampId(campId);        // Residents registered for this camp
    }

    public List<Screening> screenings(Long campId) {
        return screeningRepository.findByBeneficiaryCampId(campId); // Screening results of this camp
    }

    public CampBeneficiary getBeneficiary(Long id) {
        return beneficiaryRepository.findById(id).orElseThrow();
    }

    //Actions
    // Assign a health worker to a camp 
    public void assignStaff(Long campId, Long workerId, String workerName, StaffRole role, String duty) {
        CampStaff staff = new CampStaff();
        staff.setCamp(get(campId));                 
        staff.setWorkerId(workerId);                
        staff.setWorkerName(workerName);            
        staff.setRole(role);
        staff.setDuty(duty);
        staffRepository.save(staff);                
    }

    // Register a resident for a camp, throws if they are already registered
    public void registerBeneficiary(Long campId, Long residentId, String name,
                                    String village, Integer age, String gender) {
        if (beneficiaryRepository.existsByCampIdAndResidentId(campId, residentId)) {   // Duplicate check
            throw new IllegalStateException("Resident " + residentId + " is already registered for this camp");
        }
        CampBeneficiary b = new CampBeneficiary();
        b.setCamp(get(campId));
        b.setResidentId(residentId);                
        b.setResidentName(name);                    
        b.setVillage(village);
        b.setAge(age);
        b.setGender(gender);
        beneficiaryRepository.save(b);              
    }
}