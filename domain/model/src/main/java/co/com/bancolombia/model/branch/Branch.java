package co.com.bancolombia.model.branch;

import co.com.bancolombia.model.branch.exceptions.InvalidBranchException;

import java.util.Optional;

public record Branch(String id, Optional<String> name) {
    public Branch {
        name.filter(optName -> !optName.isBlank())
                .orElseThrow(() -> new InvalidBranchException("Branch name must not be empty"));
    }
}
