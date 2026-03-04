package tn.english.school.backend.exceptions;

public class DuplicateClaimException extends RuntimeException {

  public DuplicateClaimException(String message) {
    super(message);
  }
}