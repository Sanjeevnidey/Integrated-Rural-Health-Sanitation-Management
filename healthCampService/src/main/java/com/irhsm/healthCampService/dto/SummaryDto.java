package com.irhsm.healthCampService.dto;

public record SummaryDto(
        long total,          // Number of camps
        long pending,        // Number of pending follow ups
        long upcoming,       // Camps from today onwards
        long beneficiaries,  // Residents registered across all camps
        long abnormal        // Screenings that were ABNORMAL or CRITICAL
) {}
