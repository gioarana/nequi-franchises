package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public class RenameBranchService {

    private final FranchiseRepository repository;

    public RenameBranchService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<Branch> rename(UUID franchiseId, UUID branchId, String name) {
        return Mono.defer(() -> repository.update(franchiseId, current -> {
            Branch target = current.branches().stream().filter(branch -> branch.id().equals(branchId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada: " + branchId));

            Branch updatedBranch = new Branch(target.id(), name, target.products());

            List<Branch> branches = current.branches().stream()
                    .map(branch -> branch.id().equals(branchId) ? updatedBranch : branch)
                    .toList();

            return new Franchise(current.id(), current.name(), branches);
            })
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Franquicia no encontrada: " + franchiseId)))
                .map(updated -> updated.branches().stream()
                                .filter(branch -> branch.id().equals(branchId))
                                .findFirst()
                                .orElseThrow())
        );
    }
}