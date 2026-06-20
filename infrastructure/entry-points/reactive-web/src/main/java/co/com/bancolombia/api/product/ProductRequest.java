package co.com.bancolombia.api.product;

import java.math.BigInteger;

public record ProductRequest(String name, BigInteger stock) {}
