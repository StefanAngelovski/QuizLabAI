package com.lab24.quizlabai.service;

import com.lab24.quizlabai.model.Professor;
import com.lab24.quizlabai.model.Student;
import com.lab24.quizlabai.model.Subject;

import java.util.List;

public interface SubjectService {
    void saveSubject(Subject subject, String professorUsername);
    void updateSubject(Long id, Subject updatedSubject);
    void deleteSubject(Long id);
    Subject findById(Long id);
    List<Subject> findSubjectsCreatedBy(String username);
    List<Student> findStudentsForProfessor(Professor professor, String search);
    void addStudentToSubject(Long subjectId, String studentUsername);
    void removeStudentFromSubject(Long subjectId, String studentUsername);
}
