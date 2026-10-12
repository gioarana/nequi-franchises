package com.nequi.franchises.adapter.in.web;

import com.nequi.franchises.adapter.in.web.dto.BranchResponse;
import com.nequi.franchises.adapter.in.web.dto.CreateBranchRequest;
import com.nequi.franchises.adapter.in.web.dto.UpdateNameRequest;
import com.nequi.franchises.application.service.AddBranchService;
import com.nequi.franchises.application.service.RenameBranchService;
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
    private final RenameBranchService renameBranchService;

    public BranchController(AddBranchService service, RenameBranchService renameBranchService) {
        this.service = service;
        this.renameBranchService = renameBranchService;
    }

    @PostMapping
    public Mono<ResponseEntity<BranchResponse>> add(@PathVariable("franchiseId") UUID franchiseId,
                                                    @Valid @RequestBody CreateBranchRequest request) {
        return service.add(franchiseId, request.name())
                .map(branch -> {
                    BranchResponse response = BranchResponse.from(franchiseId, branch);

                    URI location = URI.create("/api/v1/franchises/" + franchiseId + "/branches/" + branch.id());

                    return ResponseEntity.created(location).body(response);
                });
    }

    @PatchMapping("/{branchId}/name")
    public Mono<ResponseEntity<BranchResponse>> rename(@PathVariable("franchiseId") UUID franchiseId,
                                                       @PathVariable("branchId") UUID branchId,
                                                       @Valid @RequestBody UpdateNameRequest request) {
        return renameBranchService.rename(franchiseId, branchId, request.name())
                .map(branch -> ResponseEntity.ok(BranchResponse.from(franchiseId, branch)));
    }
}