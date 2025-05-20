package com.lab24.quizlabai.web;

import com.lab24.quizlabai.model.Subject;
import com.lab24.quizlabai.service.SubjectService;
import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.enums.SidebarItem;
import com.lab24.quizlabai.service.UserService;
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
    private final UserService userService;

    public SubjectController(SubjectService subjectService, UserService userService) {
        this.subjectService = subjectService;
        this.userService = userService;
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
    @PostMapping("/{subjectId}/add-student")
    public String addStudentToSubject(@PathVariable Long subjectId,
                                      @RequestParam String studentUsername,
                                      @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        subjectService.addStudentToSubject(subjectId, studentUsername);
        return "redirect:/subjects/management";
    }
    @GetMapping("/{id}/assign-student")
    public String showAssignStudentPage(@AuthenticationPrincipal User user, @PathVariable Long id, Model model) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }
        Subject subject = subjectService.findById(id);
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(user.getRole());
        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("username", user.getUsername());
        List<User> allStudents = userService.findAllStudents();
        model.addAttribute("subject", subject);
        model.addAttribute("allStudents", allStudents);
        return "studentAssignToSubject";
    }
    @PostMapping("/{subjectId}/remove-student")
    public String removeStudentFromSubject(@PathVariable Long subjectId,
                                           @RequestParam String studentUsername,
                                           @AuthenticationPrincipal User user) {
        if (user.getRole() != Role.ROLE_PROFESSOR) {
            return "redirect:/access-denied";
        }

        subjectService.removeStudentFromSubject(subjectId, studentUsername);
        return "redirect:/subjects/management";
    }

}
