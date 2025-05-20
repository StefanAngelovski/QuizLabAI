package com.lab24.quizlabai.model.exceptions;

public class MaximumFileSizeException extends RuntimeException {
    public MaximumFileSizeException(double maxFileSize) {
        super("File size exceeds maximum file size of " + maxFileSize + "MB");
    }
}
