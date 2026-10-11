package com.nequi.franchises.adapter.in.web;

import com.nequi.franchises.adapter.in.web.dto.BranchResponse;
import com.nequi.franchises.adapter.in.web.dto.CreateBranchRequest;
import com.nequi.franchises.application.service.AddBranchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/franchises/{franchiseId}/branches")
public class BranchController {

    private final AddBranchService service;

    public BranchController(AddBranchService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<BranchResponse>> add(
            @PathVariable("franchiseId") UUID franchiseId,
            @Valid @RequestBody CreateBranchRequest request
    ) {
        return service.add(franchiseId, request.name())
                .map(branch -> {
                    BranchResponse response =
                            BranchResponse.from(franchiseId, branch);

                    URI location = URI.create(
                            "/api/v1/franchises/" + franchiseId
                                    + "/branches/" + branch.id()
                    );

                    return ResponseEntity.created(location)
                            .body(response);
                });
    }
}