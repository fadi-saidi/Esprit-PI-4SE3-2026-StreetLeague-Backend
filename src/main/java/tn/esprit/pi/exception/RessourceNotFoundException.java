package tn.esprit.pi.exception;

public class RessourceNotFoundException extends RuntimeException {

    public RessourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }
}
