package com.lab24.quizlabai.model.exceptions;

public class ProfilePictureNotFoundException extends RuntimeException {
    public ProfilePictureNotFoundException() {
        super("Could not load the profile picture");
    }
}
