package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveProductUseCase {

    private final ProductRepository productRepository;

}
