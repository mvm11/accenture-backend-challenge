package co.com.bancolombia.model.franchise.exceptions;

public class InvalidFranchiseException extends RuntimeException {
    public InvalidFranchiseException(String message) {
        super(message);
    }
}