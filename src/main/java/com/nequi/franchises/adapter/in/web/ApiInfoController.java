package com.nequi.franchises.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class ApiInfoController {

    @GetMapping
    public Mono<ApiInfoResponse> info() {
        return Mono.just(
                new ApiInfoResponse("nequi-franchises", "v1")
        );
    }

    public record ApiInfoResponse(String name, String apiVersion) {
    }
}