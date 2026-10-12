package com.nequi.franchises.configuration;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.application.service.*;
import org.springframework.beans.factory.BeanRegistrarDslMarker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public CreateFranchiseService createFranchiseService(FranchiseRepository repository) {
        return new CreateFranchiseService(repository);
    }

    @Bean
    public AddBranchService addBranchService(FranchiseRepository repository) {
        return new AddBranchService(repository);
    }

    @Bean
    public AddProductService addProductService(FranchiseRepository repository) {
        return new AddProductService(repository);
    }

    @Bean
    public UpdateProductStockService updateProductStockService(FranchiseRepository repository){
        return new UpdateProductStockService(repository);
    }

    @Bean
    public DeleteProductService deleteProductService(FranchiseRepository repository){
        return new DeleteProductService(repository);
    }

    @Bean
    public FindMaxStockProductsService findMaxStockProductsService(FranchiseRepository repository){
        return new FindMaxStockProductsService(repository);
    }
}