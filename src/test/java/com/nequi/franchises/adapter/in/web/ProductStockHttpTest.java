package com.nequi.franchises.adapter.in.web;

import com.mongodb.MongoTimeoutException;
import com.mongodb.reactivestreams.client.MongoClient;
import com.nequi.franchises.adapter.out.persistence.repository.SpringDataFranchiseRepository;
import com.nequi.franchises.application.service.AddProductService;
import com.nequi.franchises.application.service.UpdateProductStockService;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ProductStockHttpTest {

    private static final String PRODUCTS_PATH =
            "/api/v1/franchises/"
                    + "00000000-0000-0000-0000-000000000001"
                    + "/branches/"
                    + "00000000-0000-0000-0000-000000000002"
                    + "/products";

    @LocalServerPort
    private int port;

    @MockitoBean
    private MongoClient mongoClient;

    @MockitoBean
    private SpringDataFranchiseRepository repository;

    @MockitoBean
    private AddProductService addProductService;

    @MockitoBean
    private UpdateProductStockService updateProductStockService;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    void shouldRejectFractionalStockWhenCreatingProduct() {
        client.post()
                .uri(PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Café",
                          "stock": 1.5
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.title").isEqualTo("Solicitud inválida");

        verifyNoInteractions(addProductService, repository);
    }

    @Test
    void shouldRejectFractionalStockWhenUpdatingProduct() {
        client.patch()
                .uri(PRODUCTS_PATH
                        + "/00000000-0000-0000-0000-000000000003/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "stock": 1.5
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.title").isEqualTo("Solicitud inválida");

        verifyNoInteractions(updateProductStockService, repository);
    }

    @Test
    void shouldDescribeMissingStockInSpanish() {
        client.post()
                .uri(PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Café"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.title").isEqualTo("Error de validación")
                .jsonPath("$.errors[0].field").isEqualTo("stock")
                .jsonPath("$.errors[0].message")
                .isEqualTo("La cantidad de stock es obligatoria");

        verifyNoInteractions(addProductService, repository);
    }

    @Test
    void shouldReturnNotFoundAsProblemDetail() {
        when(addProductService.add(
                any(UUID.class),
                any(UUID.class),
                anyString(),
                anyInt()
        )).thenReturn(Mono.error(
                new ResourceNotFoundException("Sucursal no encontrada")
        ));

        client.post()
                .uri(PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Café",
                          "stock": 20
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader()
                .contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.title").isEqualTo("Recurso no encontrado")
                .jsonPath("$.detail").isEqualTo("Sucursal no encontrada");
    }

    @Test
    void shouldReturnConflictWithoutExposingInternalMessage() {
        when(addProductService.add(
                any(UUID.class),
                any(UUID.class),
                anyString(),
                anyInt()
        )).thenReturn(Mono.error(
                new OptimisticLockingFailureException("Detalle interno")
        ));

        client.post()
                .uri(PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Café",
                          "stock": 20
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader()
                .contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.title")
                .isEqualTo("Modificación concurrente")
                .jsonPath("$.detail").isEqualTo(
                        "La franquicia fue modificada por otra solicitud. "
                                + "Intenta nuevamente la operación."
                );
    }

    @Test
    void shouldReturnUnavailableWithoutExposingInternalMessage() {
        when(addProductService.add(
                any(UUID.class),
                any(UUID.class),
                anyString(),
                anyInt()
        )).thenReturn(Mono.error(
                new MongoTimeoutException("Detalle interno")
        ));

        client.post()
                .uri(PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Café",
                          "stock": 20
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader()
                .contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.title")
                .isEqualTo("Persistencia no disponible")
                .jsonPath("$.detail").isEqualTo(
                        "No fue posible acceder a la persistencia. "
                                + "Intenta nuevamente más tarde."
                );
    }
}