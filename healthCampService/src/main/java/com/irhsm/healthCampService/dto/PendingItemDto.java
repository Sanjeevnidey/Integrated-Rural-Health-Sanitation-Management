package com.irhsm.healthCampService.dto;

import java.time.LocalDate;

public record PendingItemDto(
        String source,      // w hich service it came from: always "HEALTH_CAMP"
        Long id,            // Followup id, the dashboard sends it back in PUT /{id}/status
        String title,       // short description shown on the dashboard
        String village,     // village of the resident
        String status,      // PENDING, DONE, REFERRED, MISSED
        String priority,    // HIGH, MEDIUM, LOW
        LocalDate date      // due date
) {}