package com.irhsm.healthCampService.dto;

public record ResidentDto(
        Long id,
        String name,
        String village,
        Integer age,
        String gender
) {}