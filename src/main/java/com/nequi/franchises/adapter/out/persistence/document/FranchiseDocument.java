package com.nequi.franchises.adapter.out.persistence.document;

import com.nequi.franchises.domain.model.Branch;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "franchises")
public class FranchiseDocument {

    @Id
    private String id;

    private String name;

    private List<Branch> branches;

    @Version
    private Long version;

    public FranchiseDocument() {
    }

    public FranchiseDocument(
            String id,
            String name,
            List<Branch> branches
    ) {
        this.id = id;
        this.name = name;
        this.branches = List.copyOf(branches);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public Long getVersion() {
        return version;
    }
}