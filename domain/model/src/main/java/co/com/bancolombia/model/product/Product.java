package co.com.bancolombia.model.product;

import co.com.bancolombia.model.product.exception.InvalidProductException;

import java.math.BigInteger;


public record Product(String id, String name, BigInteger stock) {
    public Product {

        if (name == null || name.isEmpty()) {
            throw new InvalidProductException("Product name must not be empty");
        }

        if (stock == null) {
            throw new InvalidProductException("Product stock must not be null");
        }
    }
}