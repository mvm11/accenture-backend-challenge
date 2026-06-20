package co.com.bancolombia.model.branch;

import java.math.BigInteger;

public record TopStockProduct(String branchId, String branchName, String productId, String productName, BigInteger stock) {}
