package com.lab24.quizlabai.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String username) {
        super(String.format("The email %s is already taken", username));
    }
}
