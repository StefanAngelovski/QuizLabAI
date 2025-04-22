package com.lab24.quizlabai.web;


import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.enums.SidebarItem;
import com.lab24.quizlabai.service.QuizService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

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
}
