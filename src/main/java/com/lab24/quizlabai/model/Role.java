package com.lab24.quizlabai.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ADMIN, ROLE_STUDENT, ROLE_PROFESSOR;

    @Override
    public String getAuthority() {
        return name();
    }
}

