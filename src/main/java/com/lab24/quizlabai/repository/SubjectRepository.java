package com.lab24.quizlabai.repository;

import com.lab24.quizlabai.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByCreatorUsername(String username);
    Optional<Subject> findByName(String name);
}

