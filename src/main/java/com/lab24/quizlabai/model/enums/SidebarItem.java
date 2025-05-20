package com.lab24.quizlabai.model.enums;

import com.lab24.quizlabai.model.Role;

import java.util.ArrayList;
import java.util.List;

public enum SidebarItem {
    DASHBOARD("Dashboard", "🎛️", "/dashboard", Role.ROLE_STUDENT, Role.ROLE_PROFESSOR, Role.ROLE_ADMIN),
    MY_GRADES("My Grades", "🎓", "/dashboard", Role.ROLE_STUDENT),
    GRADES("Grades", "📊", "/dashboard", Role.ROLE_PROFESSOR),
    AVAILABLE_QUIZZES("Available Quizzes", "📝", "/available-quizzes", Role.ROLE_STUDENT),
    CREATE_QUIZ("Create Quiz", "📝", "/quizgeneration", Role.ROLE_PROFESSOR),
    SUBMITTED_QUIZZES("Submitted Quizzes", "📥️", "/submitted-quizzes", Role.ROLE_PROFESSOR),
    MANAGE_QUIZZES("Manage Quizzes", "✏️", "/quiz-management", Role.ROLE_PROFESSOR),
    MANAGE_SUBJECTS("Manage Subjects", "️📚", "/subject-management", Role.ROLE_PROFESSOR),
    COMPLETED_QUIZZES("Completed Quizzes", "✅", "/completed-quizzes", Role.ROLE_STUDENT),
    PROFESSOR_FEEDBACK("Professor Feedback", "💬", "/dashboard", Role.ROLE_STUDENT),
    STUDY_RESOURCES("Study Materials", "📚", "/study-materials", Role.ROLE_STUDENT, Role.ROLE_STUDENT),
    REPORTS("Reports & Analytics", "📈", "/dashboard", Role.ROLE_PROFESSOR),
    STUDENTS("Students", "👥", "/students", Role.ROLE_PROFESSOR),
    STATISTICS("Statistics", "🧮", "/dashboard", Role.ROLE_PROFESSOR),
    SETTINGS("Settings", "⚙️", "/profile", Role.ROLE_PROFESSOR, Role.ROLE_STUDENT);

    private final String title;
    private final String emoji;
    private final String endpoint;
    private final Role[] roles;

    SidebarItem(String title, String emoji, String endpoint, Role... roles) {
        this.title = title;
        this.emoji = emoji;
        this.endpoint = endpoint;
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

    public String getTitle() {
        return this.title;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public String getEndpoint() {
        return this.endpoint;
    }
}
