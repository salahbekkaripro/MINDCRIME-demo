package fr.miage.mindcrime.api.rest;
import fr.miage.mindcrime.moteur.modele.Partie;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ErreursApi {
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail introuvable(NoSuchElementException erreur) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, erreur.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalide(IllegalArgumentException erreur) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, erreur.getMessage());
    }
    @ExceptionHandler(Partie.PartieFermee.class)
    public ProblemDetail conflit(Partie.PartieFermee erreur) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, erreur.getMessage());
    }
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail concurrente() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "La partie a changé. Actualise avant de réessayer.");
    }
}
