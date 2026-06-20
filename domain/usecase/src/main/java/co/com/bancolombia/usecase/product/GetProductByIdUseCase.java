package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;
}
