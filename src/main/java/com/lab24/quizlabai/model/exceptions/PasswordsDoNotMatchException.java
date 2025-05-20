package com.lab24.quizlabai.model.exceptions;

public class PasswordsDoNotMatchException extends RuntimeException {
    public PasswordsDoNotMatchException() {
        super("The passwords don't match!");
    }
}


