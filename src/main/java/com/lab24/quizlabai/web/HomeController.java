package com.lab24.quizlabai.web;


import com.lab24.quizlabai.dto.QuizRequestDto;
import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.enums.SidebarItem;
import com.lab24.quizlabai.service.QuizService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {
    private final QuizService quizService;

    public HomeController(QuizService quizService) {
        this.quizService = quizService;
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
        Role role = user.getRole();
        List<Quiz> quizzes = quizService.getAllQuizzes();
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(role);

        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("quizzes", quizzes);
        return "quizManagement";
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
        List<String> questionTypes = Arrays.asList("Multiple Choice", "Fixed Choice", "Short Answer");

        model.addAttribute("questionTypes", questionTypes);
        model.addAttribute("username", user.getUsername());
        return "quizGeneration";
    }

    @GetMapping("/edit-quiz/{id}")
    public String editQuiz(@PathVariable("id") Long id, Model model) {
        Optional<Quiz> quizOptional = Optional.ofNullable(quizService.getQuizById(id));
        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();
            if (quiz.getQuestionTypes() == null) {
                quiz.setQuestionTypes(new ArrayList<>());
            }
            model.addAttribute("quiz", quiz);
            return "quizEdit";
        } else {
            return "redirect:/quiz-management";
        }
    }

    @PostMapping("/edit-quiz/{id}")
    public String saveEditedQuiz(
            @PathVariable("id") Long id,
            @ModelAttribute QuizRequestDto requestDto,
            @RequestParam(value = "pdfFile", required = false) MultipartFile file
    ) {
        try {
            quizService.updateQuiz(id, requestDto, file);
            return "redirect:/quiz-management";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/delete-quiz/{id}")
    public String deleteQuiz(@PathVariable("id") Long id) {
        quizService.deleteQuiz(id);
        return "redirect:/quiz-management";
    }
}
