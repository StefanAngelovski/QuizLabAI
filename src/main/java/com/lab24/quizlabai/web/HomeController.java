package com.lab24.quizlabai.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String showHomePage() {
        return "index";
    }

    @GetMapping({"/statistics"})
    public String showStatisticsPage() {
        return "statistics";
    }

    @GetMapping({"/quiz-management"})
    public String showQuizManagement() {
        return "quizManagement";
    }

    @GetMapping({"/dashboard"})
    public String showDashboard() {
        return "dashboard";
    }

    @GetMapping({"/student-progress"})
    public String showStudentProgress() {
        return "studentProgressPage";
    }

    @GetMapping({"/profile"})
    public String showProfile() {
        return "UserProfile";
    }

}
