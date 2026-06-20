package co.com.bancolombia.api.product;

import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.usecase.product.DeleteProductUseCase;
import co.com.bancolombia.usecase.product.GetProductByIdUseCase;
import co.com.bancolombia.usecase.product.SaveProductUseCase;
import co.com.bancolombia.usecase.product.UpdateProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final SaveProductUseCase saveProductUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return request.bodyToMono(ProductRequest.class)
                .map(dto -> new Product(null, dto.name(), dto.stock()))
                .flatMap(product -> saveProductUseCase.run(branchId, product))
                .flatMap(saved -> ServerResponse.status(201).bodyValue(saved));
    }

    public Mono<ServerResponse> getProductById(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("id");
        return getProductByIdUseCase.run(franchiseId, branchId, productId)
                .flatMap(product -> ServerResponse.ok().bodyValue(product))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("id");
        return request.bodyToMono(ProductRequest.class)
                .flatMap(dto -> updateProductUseCase.updateName(franchiseId, branchId, productId, dto.name()))
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("id");
        return request.bodyToMono(ProductRequest.class)
                .flatMap(dto -> updateProductUseCase.updateStock(franchiseId, branchId, productId, dto.stock()))
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("id");
        return deleteProductUseCase.run(franchiseId, branchId, productId)
                .flatMap(deleted -> ServerResponse.ok().bodyValue(deleted))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
