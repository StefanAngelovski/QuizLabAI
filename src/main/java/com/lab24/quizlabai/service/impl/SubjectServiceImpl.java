package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.model.Professor;
import com.lab24.quizlabai.model.Student;
import com.lab24.quizlabai.model.Subject;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.repository.SubjectRepository;
import com.lab24.quizlabai.repository.UserRepository;
import com.lab24.quizlabai.service.SubjectService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void saveSubject(Subject subject, String professorUsername) {
        User user = userRepository.findByUsername(professorUsername).orElseThrow();
        if (!(user instanceof Professor)) {
            throw new IllegalArgumentException("User is not a professor.");
        }
        Professor professor = (Professor) user;
        subject.setCreator(professor);
        subjectRepository.save(subject);
        professor.addTeachingSubject(subject);
        userRepository.save(professor);
    }

    @Override
    public void updateSubject(Long id, Subject updatedSubject) {
        Subject subject = subjectRepository.findById(id).orElseThrow();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!subject.getCreator().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the creator of this subject.");
        }

        subject.setName(updatedSubject.getName());
        subject.setDescription(updatedSubject.getDescription());
        subjectRepository.save(subject);
    }

    @Override
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id).orElseThrow();
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!subject.getCreator().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the creator of this subject.");
        }

        for (Professor professor : subject.getProfessors()) {
            professor.removeTeachingSubject(subject);
            userRepository.save(professor); // Save professor after removing the subject
        }


        subjectRepository.deleteById(id);
    }


    @Override
    public Subject findById(Long id) {
        return subjectRepository.findById(id).orElseThrow();
    }

    @Override
    public List<Subject> findSubjectsCreatedBy(String username) {
        return subjectRepository.findByCreatorUsername(username);
    }
    @Override
    public void addStudentToSubject(Long subjectId, String studentUsername) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() ->
                new IllegalArgumentException("Subject not found"));
        User user = userRepository.findByUsername(studentUsername).orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        if (!(user instanceof Student)) {
            throw new IllegalArgumentException("User is not a student");
        }

        Student student = (Student) user;


        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!subject.getCreator().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the creator of this subject.");
        }

        student.enrollInSubject(subject);
        userRepository.save(student);
    }

    @Override
    public void removeStudentFromSubject(Long subjectId, String studentUsername) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() ->
                new IllegalArgumentException("Subject not found"));
        User user = userRepository.findByUsername(studentUsername).orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        if (!(user instanceof Student)) {
            throw new IllegalArgumentException("User is not a student");
        }

        Student student = (Student) user;

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!subject.getCreator().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the creator of this subject.");
        }

        student.removeFromSubject(subject);
        userRepository.save(student);
    }
}
