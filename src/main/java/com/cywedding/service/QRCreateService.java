package com.cywedding.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class QRCreateService {
    
    private final RestClient restClient;

    public QRCreateService(RestClient restClient) {
        this.restClient = restClient;
    }
}
