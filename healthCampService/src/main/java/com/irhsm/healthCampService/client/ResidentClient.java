package com.irhsm.healthCampService.client;

import com.irhsm.healthCampService.dto.ResidentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component   //Spring creates it and injects it into the controller
public class ResidentClient {

    private final RestTemplate restTemplate;    // HTTP client from AppConfig
    private final String baseUrl;   // http://localhost:8081 from application.properties

    public ResidentClient(RestTemplate restTemplate,
                          @Value("${resident.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // GET /api/residents/{id} - resident details as a Java object
    // Throws HttpClientErrorException.NotFound if the ID doesn't exist,
    // or RestClientException if the service is down; the controller handles both
    public ResidentDto getResident(Long id) {
        return restTemplate.getForObject(baseUrl + "/api/residents/" + id, ResidentDto.class);
    }
}