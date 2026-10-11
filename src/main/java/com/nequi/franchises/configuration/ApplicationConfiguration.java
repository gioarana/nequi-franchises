package com.nequi.franchises.configuration;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.application.service.AddBranchService;
import com.nequi.franchises.application.service.CreateFranchiseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public CreateFranchiseService createFranchiseService(
            FranchiseRepository repository
    ) {
        return new CreateFranchiseService(repository);
    }

    @Bean
    public AddBranchService addBranchService(
            FranchiseRepository repository
    ) {
        return new AddBranchService(repository);
    }
}