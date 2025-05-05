package com.lab24.quizlabai.web;

import com.lab24.quizlabai.model.Subject;
import com.lab24.quizlabai.service.SubjectService;
import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.enums.SidebarItem;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/management")
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

    @GetMapping("/create")
    public String showCreateSubjectPage(Model model, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        model.addAttribute("subject", new Subject());
        return "createSubject"; // This will render the createSubject.html page
    }

    @PostMapping("/add")
    public String saveSubject(@ModelAttribute Subject subject, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        subjectService.saveSubject(subject, user.getUsername());
        return "redirect:/subjects/management";
    }

    @GetMapping("/edit/{id}")
    public String showEditSubjectPage(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        Optional<Subject> existingSubjectOptional = Optional.ofNullable(subjectService.findById(id));
        if (existingSubjectOptional.isPresent()) {
            Subject existingSubject = existingSubjectOptional.get();
            if (!existingSubject.getCreator().getUsername().equals(user.getUsername())) {
                return "redirect:/access-denied";
            }
            model.addAttribute("subject", existingSubject);
        } else {
            return "redirect:/subjects/management"; // or an error page
        }

        return "editSubject"; // This will render the editSubject.html page
    }

    // Handle the form submission
    @PostMapping("/edit/{id}")
    public String saveEditedSubject(@PathVariable Long id, @ModelAttribute Subject subject, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        Optional<Subject> existingSubjectOptional = Optional.ofNullable(subjectService.findById(id));
        if (existingSubjectOptional.isPresent()) {
            Subject existingSubject = existingSubjectOptional.get();
            if (!existingSubject.getCreator().getUsername().equals(user.getUsername())) {
                return "redirect:/access-denied";
            }
            subject.setId(id); // Ensure the ID is preserved
            subjectService.saveSubject(subject, user.getUsername());
        } else {
            return "redirect:/subjects/management"; // or an error page
        }

        return "redirect:/subjects/management";
    }



        @PostMapping("/delete/{id}")
    public String deleteSubject(@PathVariable("id") Long id, @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        subjectService.deleteSubject(id);
        return "redirect:/subjects/management";
    }
}
