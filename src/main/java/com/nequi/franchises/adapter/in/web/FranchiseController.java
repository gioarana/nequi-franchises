package com.nequi.franchises.adapter.in.web;

import com.nequi.franchises.adapter.in.web.dto.CreateFranchiseRequest;
import com.nequi.franchises.adapter.in.web.dto.FranchiseResponse;
import com.nequi.franchises.application.service.CreateFranchiseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/franchises")
public class FranchiseController {

    private final CreateFranchiseService service;

    public FranchiseController(CreateFranchiseService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<FranchiseResponse>> create(
            @Valid @RequestBody CreateFranchiseRequest request
    ) {
        return service.create(request.name())
                .map(franchise -> {
                    FranchiseResponse response =
                            FranchiseResponse.from(franchise);

                    URI location = URI.create(
                            "/api/v1/franchises/" + franchise.id()
                    );

                    return ResponseEntity.created(location)
                            .body(response);
                });
    }
}