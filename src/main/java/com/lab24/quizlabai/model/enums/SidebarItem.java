package com.lab24.quizlabai.model.enums;

import com.lab24.quizlabai.model.Role;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public enum SidebarItem {
    MY_GRADES("My Grades", "🎓", Role.ROLE_STUDENT),
    GRADES("Grades", "📊", Role.ROLE_PROFESSOR),
    AVAILABLE_QUIZZES("Available Quizzes", "📝", Role.ROLE_STUDENT),
    CREATE_QUIZ("Create Quiz", "📝", Role.ROLE_PROFESSOR),
    SUBMITTED_QUIZZES("Submitted Quizzes", "📥️", Role.ROLE_PROFESSOR),
    MANAGE_QUIZZES("Manage Quizzes", "✏️", Role.ROLE_PROFESSOR),
    COMPLETED_QUIZZES("Completed Quizzes", "✅", Role.ROLE_STUDENT),
    PROFESSOR_FEEDBACK("Professor Feedback", "💬", Role.ROLE_STUDENT),
    STUDY_RESOURCES("Study Resources", "📚", Role.ROLE_STUDENT, Role.ROLE_STUDENT),
    REPORTS("Reports & Analytics", "📈", Role.ROLE_PROFESSOR),
    STUDENTS("Students", "👥", Role.ROLE_PROFESSOR),
    STATISTICS("Statistics", "🧮", Role.ROLE_PROFESSOR),
    EDIT_QUIZ("Edit Quiz", "📝", Role.ROLE_PROFESSOR),
    SETTINGS("Settings", "⚙️", Role.ROLE_PROFESSOR, Role.ROLE_STUDENT);

    @Getter
    private final String title;
    @Getter
    private final String emoji;
    private final Role[] roles;

    SidebarItem(String title, String emoji, Role... roles) {
        this.title = title;
        this.emoji = emoji;
        this.roles = roles;
    }

    public boolean isVisibleForRole(Role role) {
        for (Role allowedRole : roles) {
            if (allowedRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    public static List<SidebarItem> getVisibleItems(Role role) {
        List<SidebarItem> visibleItems = new ArrayList<>();
        for (SidebarItem item : values()) {
            if (item.isVisibleForRole(role)) {
                visibleItems.add(item);
            }
        }
        return visibleItems;
    }
}
