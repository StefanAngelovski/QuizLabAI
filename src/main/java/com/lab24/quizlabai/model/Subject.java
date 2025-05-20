package com.lab24.quizlabai.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subject")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "teachingSubjects")
    private Set<Professor> professors = new HashSet<>();

    @ManyToMany(mappedBy = "enrolledSubjects")
    private Set<Student> students = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Professor creator;

    // Constructors
    public Subject() {}

    public Subject(String name, String description, Professor creator) {
        this.name = name;
        this.description = description;
        this.creator = creator;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Professor> getProfessors() {
        return professors;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public Professor getCreator() {
        return creator;
    }

    public void setCreator(Professor creator) {
        this.creator = creator;
    }
}
