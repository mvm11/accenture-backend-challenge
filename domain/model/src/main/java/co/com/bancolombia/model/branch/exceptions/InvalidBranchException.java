package co.com.bancolombia.model.branch.exceptions;

public class InvalidBranchException extends RuntimeException {
    public InvalidBranchException(String message) {
        super(message);
    }
}
