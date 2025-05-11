package com.lab24.quizlabai.web;


import com.lab24.quizlabai.dto.QuizRequestDto;
import com.lab24.quizlabai.model.*;
import com.lab24.quizlabai.model.enums.SidebarItem;
import com.lab24.quizlabai.service.QuizService;

import com.lab24.quizlabai.service.SubjectService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
public class HomeController {
    private final QuizService quizService;
    private final SubjectService subjectService;

    public HomeController(QuizService quizService, SubjectService subjectService) {
        this.quizService = quizService;
        this.subjectService = subjectService;
    }

    @GetMapping({"/", "/home", "/index"})
    public String getHome() {
        return "index";
    }

    @GetMapping({"/statistics"})
    public String showStatisticsPage() {
        return "statistics";
    }

    @GetMapping({"/quiz-management"})
    public String showQuizManagement(Model model, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        Professor professor = (Professor) user;
        List<Quiz> quizzes = quizService.getQuizzesByCreator(professor);
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(user.getRole());

        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("quizzes", quizzes);
        return "quizManagement";
    }

    @GetMapping("/subject-management")
    public String showSubjectManagementPage(Model model, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        List<Subject> subjects = subjectService.findSubjectsCreatedBy(user.getUsername());
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(user.getRole());

        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("subjects", subjects);
        return "subjectManagement";
    }

    @GetMapping({"/dashboard"})
    public String showDashboard(Model model, @AuthenticationPrincipal User user) {
        Role role = user.getRole();
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(role);

        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        return "dashboard";
    }

    @GetMapping({"/student-progress"})
    public String showStudentProgress(Model model, @AuthenticationPrincipal User user) {
        Role role = user.getRole();
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(role);

        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        return "studentProgressPage";
    }

    @GetMapping({"/quizgeneration"})
    public String showQuizGeneratorPage(Model model, @AuthenticationPrincipal User user) {
        Role role = user.getRole();
        if (role != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        List<Subject> subjects = subjectService.findSubjectsCreatedBy(user.getUsername());
        List<String> questionTypes = Arrays.asList("Multiple Choice", "Fixed Choice", "Short Answer");

        model.addAttribute("subjects", subjects);
        model.addAttribute("questionTypes", questionTypes);
        model.addAttribute("username", user.getUsername());

        return "quizGeneration";
    }

    @GetMapping("/edit-quiz/{id}")
    public String editQuiz(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        Optional<Quiz> quizOptional = Optional.ofNullable(quizService.getQuizById(id));
        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();
            List<Subject> subjects = subjectService.findSubjectsCreatedBy(user.getUsername());
            if (!quiz.getCreator().getUsername().equals(user.getUsername())) {
                return "redirect:/access-denied";
            }

            if (quiz.getQuestionTypes() == null) {
                quiz.setQuestionTypes(new ArrayList<>());
            }
            model.addAttribute("quiz", quiz);
            model.addAttribute("subjects", subjects);

            return "quizEdit";
        } else {
            return "redirect:/quiz-management";
        }
    }

    @PostMapping("/edit-quiz/{id}")
    public String saveEditedQuiz(
            @PathVariable("id") Long id,
            @ModelAttribute QuizRequestDto requestDto,
            @RequestParam(value = "pdfFile", required = false) MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        Optional<Quiz> quizOptional = Optional.ofNullable(quizService.getQuizById(id));
        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();


            if (!quiz.getCreator().getUsername().equals(user.getUsername())) {
                return "redirect:/access-denied";
            }
        }

        try {
            quizService.updateQuiz(id, requestDto, file);
            return "redirect:/quiz-management";
        } catch (IOException e) {
            e.printStackTrace();
            return "common/error";
        }
    }

    @PostMapping("/delete-quiz/{id}")
    public String deleteQuiz(@PathVariable("id") Long id, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        Optional<Quiz> quizOptional = Optional.ofNullable(quizService.getQuizById(id));
        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();

            if (!quiz.getCreator().getUsername().equals(user.getUsername())) {
                return "redirect:/access-denied";
            }
        }

        quizService.deleteQuiz(id);
        return "redirect:/quiz-management";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model, @AuthenticationPrincipal User user) {
        Role role = user.getRole();
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(role);

        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("errorMessage", "You do not have permission to perform this action.");

        return "common/error";
    }
    @GetMapping("/available-quizzes")
    public String showAvailableQuizzes(Model model, @AuthenticationPrincipal User user) {
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(user.getRole());
        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());

        List<Quiz> quizzes;

        if (user instanceof Student student) {
            quizzes = quizService.getQuizzesForStudent(student);
            model.addAttribute("quizzes", quizzes);
            return "availableQuizzes";
        } else {
            return "redirect:/access-denied";
        }
    }

    @GetMapping("/study-materials")
    public String showMaterials(Model model, @AuthenticationPrincipal User user) {
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(user.getRole());
        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());

        if (user instanceof Student student) {
            List<Quiz> quizzes = quizService.getQuizzesForStudent(student).stream()
                    .filter(quiz -> quiz.getPdfFile() != null && quiz.getPdfFile().length > 0)
                    .toList();
            model.addAttribute("quizzes", quizzes);
            return "studyMaterials";
        } else {
            return "redirect:/access-denied";
        }


    }

    @GetMapping("/quizzes/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Quiz quiz = quizService.getQuizById(id);

        byte[] pdfData = quiz.getPdfFile();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quiz_" + quiz.getTopic() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}

