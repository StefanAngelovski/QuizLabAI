package com.lab24.quizlabai.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name = "user_id")
public class Professor extends User {

    @ManyToMany
    @JoinTable(
            name = "professor_subjects",
            joinColumns = @JoinColumn(name = "professor_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> teachingSubjects = new HashSet<>();

    public Professor() {
        this.setRole(Role.ROLE_PROFESSOR);
    }

    public Professor(String username, String password, String email) {
        super(username, password, email, Role.ROLE_PROFESSOR);
    }

    public Set<Subject> getTeachingSubjects() {
        return teachingSubjects;
    }

    public void addTeachingSubject(Subject subject) {
        this.teachingSubjects.add(subject);
    }

    public void removeTeachingSubject(Subject subject) {
        this.teachingSubjects.remove(subject);
    }
}
