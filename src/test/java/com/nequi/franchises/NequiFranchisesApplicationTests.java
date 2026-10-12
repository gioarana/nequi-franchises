package com.nequi.franchises;

import com.mongodb.reactivestreams.client.MongoClient;
import com.nequi.franchises.adapter.out.persistence.repository.SpringDataFranchiseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class NequiFranchisesApplicationTests {

    @MockitoBean
    private MongoClient mongoClient;

    @MockitoBean
    private SpringDataFranchiseRepository repository;

    @Test
    void contextLoads() {
    }
}