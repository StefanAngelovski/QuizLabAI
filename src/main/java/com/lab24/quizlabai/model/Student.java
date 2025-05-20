package com.lab24.quizlabai.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student")
@PrimaryKeyJoinColumn(name = "user_id")
public class Student extends User {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "student_subjects",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> enrolledSubjects = new HashSet<>();

    public Student() {
        this.setRole(Role.ROLE_STUDENT);
    }

    public Student(String username, String password, String email) {
        super(username, password, email, Role.ROLE_STUDENT);
    }

    public Set<Subject> getEnrolledSubjects() {
        return enrolledSubjects;
    }

    public void enrollInSubject(Subject subject) {
        this.enrolledSubjects.add(subject);
    }

    public void removeFromSubject(Subject subject) {
        this.enrolledSubjects.remove(subject);
    }
}