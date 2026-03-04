package tn.english.school.backend.exceptions;

public class ClaimAlreadyLinkedException extends RuntimeException {

  public ClaimAlreadyLinkedException(String message) {
    super(message);
  }
}