package com.irhsm.healthCampService.dto;

// JSON body of PUT /api/camps/{id}/status - "status": "DONE", "outcome": "BP normal on recheck" }
public record StatusUpdateRequest(
        String status,      
        String outcome      
) {}