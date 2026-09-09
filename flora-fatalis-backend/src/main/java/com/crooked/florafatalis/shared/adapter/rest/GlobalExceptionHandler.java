package com.crooked.florafatalis.shared.adapter.rest;

import com.crooked.florafatalis.care.domain.CareEventNotFoundException;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdAlreadyExistsException;
import com.crooked.florafatalis.household.domain.HouseholdInvitationNotFoundException;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.domain.LocationNotFoundException;
import com.crooked.florafatalis.photo.domain.PlantPhotoNotFoundException;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.species.domain.SpeciesNotFoundException;
import com.crooked.florafatalis.user.domain.EmailAlreadyTakenException;
import com.crooked.florafatalis.user.domain.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyTakenException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  void handleEmailAlreadyTaken() {}

  @ExceptionHandler(InvalidCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  void handleInvalidCredentials() {}

  @ExceptionHandler(HouseholdAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  void handleHouseholdAlreadyExists() {}

  @ExceptionHandler(HouseholdAccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  void handleHouseholdAccessDenied() {}

  @ExceptionHandler(HouseholdInvitationNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  void handleHouseholdInvitationNotFound() {}

  @ExceptionHandler(NoActiveHouseholdException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ErrorResponse handleNoActiveHousehold() {
    return new ErrorResponse("No active household");
  }

  @ExceptionHandler(LocationNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  void handleLocationNotFound() {}

  @ExceptionHandler(SpeciesNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  void handleSpeciesNotFound() {}

  @ExceptionHandler(PlantNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  void handlePlantNotFound() {}

  @ExceptionHandler(PlantPhotoNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  void handlePlantPhotoNotFound() {}

  @ExceptionHandler(CareEventNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  void handleCareEventNotFound() {}

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ErrorResponse handleIllegalState(IllegalStateException ex) {
    return new ErrorResponse(ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
    return new ErrorResponse(ex.getMessage());
  }

  record ErrorResponse(String message) {}
}
