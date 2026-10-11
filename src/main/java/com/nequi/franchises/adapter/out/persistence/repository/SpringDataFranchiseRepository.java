package com.nequi.franchises.adapter.out.persistence.repository;

import com.nequi.franchises.adapter.out.persistence.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SpringDataFranchiseRepository
        extends ReactiveMongoRepository<FranchiseDocument, String> {
}