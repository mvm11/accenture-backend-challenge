package co.com.bancolombia.model.branch;

import co.com.bancolombia.model.branch.exceptions.InvalidBranchException;
import co.com.bancolombia.model.product.Product;

import java.util.List;
import java.util.Optional;

public record Branch(String id, Optional<String> name, List<Product> products) {
    public Branch {
        name.filter(optName -> !optName.isBlank())
                .orElseThrow(() -> new InvalidBranchException("Branch name must not be empty"));
    }
}
