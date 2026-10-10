package com.irhsm.healthCampService.service;

import com.irhsm.healthCampService.entity.CampStatus;
import com.irhsm.healthCampService.entity.HealthCamp;
import com.irhsm.healthCampService.repository.HealthCampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;              // For ordering results
import org.springframework.stereotype.Service;

import java.util.*;

@Service 
@RequiredArgsConstructor 

public class CampService {
    private final HealthCampRepository campRepository;

    // Search used by the list page; any filter left empty is ignored
    public List<HealthCamp> search(String q, String village, CampStatus status) {
        return campRepository.findAll(Sort.by("campDate").descending())          // All camps, newest date first
                .stream()
                .filter(c -> q == null || q.isBlank()     
                        || c.getName().toLowerCase().contains(q.toLowerCase()))
                .filter(c -> village == null || village.isBlank()           // Village filter
                        || c.getVillage().equalsIgnoreCase(village))
                .filter(c -> status == null || c.getStatus() == status)   // Status filter
                .toList();
    }

    // Distinct village names, for the filter dropdown
    public List<String> villages() {
        return campRepository.findAll().stream()
                .map(HealthCamp::getVillage)      // Take each camp's village
                .distinct()                       
                .sorted()                         
                .toList();
    }

    public HealthCamp get(Long id) {
        return campRepository.findById(id).orElseThrow();    // Throws if the camp doesn't exist
    }

    public HealthCamp save(HealthCamp camp) {
        return campRepository.save(camp);  // INSERT if id is null, otherwise UPDATE
    }
}
