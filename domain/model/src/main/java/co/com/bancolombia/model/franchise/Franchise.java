package co.com.bancolombia.model.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.franchise.exceptions.InvalidFranchiseException;

import java.util.List;
import java.util.Optional;


public record Franchise(String id,
                        Optional<String> name,
                        List<Branch> branches) {
    public Franchise {
        name.filter(optName -> !optName.isBlank())
                .orElseThrow(() -> new InvalidFranchiseException("Franchise name must not be empty"));
    }
}
