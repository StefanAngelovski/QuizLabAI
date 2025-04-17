package com.lab24.quizlabai.web;


import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.enums.SidebarItem;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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

    @GetMapping({"/profile"})
    public String showProfile(Model model, @AuthenticationPrincipal User user) {
        Role role = user.getRole();
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(role);

        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        return "UserProfile";
    }

}
