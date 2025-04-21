package com.lab24.quizlabai.web;

import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.enums.SidebarItem;
import com.lab24.quizlabai.model.exceptions.EmailAlreadyExistsException;
import com.lab24.quizlabai.model.exceptions.MaximumFileSizeException;
import com.lab24.quizlabai.model.exceptions.UsernameAlreadyExistsException;
import com.lab24.quizlabai.service.UserService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model,
                              @AuthenticationPrincipal User user) {
        Role role = user.getRole();
        List<SidebarItem> sidebarItems = SidebarItem.getVisibleItems(role);
        User existingUser = (User) userService.loadUserByUsername(user.getUsername());
        model.addAttribute("sidebarItems", sidebarItems);
        model.addAttribute("user", existingUser);
        model.addAttribute("username", user.getUsername());
        return "UserProfile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                @RequestParam("image") MultipartFile image,
                                @AuthenticationPrincipal User user,
                                RedirectAttributes redirectAttributes) {

        User existingUser = (User) userService.loadUserByUsername(user.getUsername());
        try {
            userService.updateUser(existingUser, updatedUser, image);
        } catch (MaximumFileSizeException | EmailAlreadyExistsException | UsernameAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        try {
            byte[] image = userService.getProfileImage(id);
            if (image == null) {
                return getDefaultAvatarImage();
            } else {
                HttpHeaders headers = new HttpHeaders();
                String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image));

                if (contentType == null) {
                    contentType = "image/jpeg";
                }
                headers.setContentType(MediaType.parseMediaType(contentType));
                return new ResponseEntity<>(image, headers, HttpStatus.OK);
            }
        } catch (Exception e) {
            return getDefaultAvatarImage();
        }
    }

    @PostMapping("/remove-image")
    public String removeProfileImage(@AuthenticationPrincipal User currentUser) {
        userService.removeProfileImage(currentUser.getId());

        return "redirect:/profile";
    }

    private ResponseEntity<byte[]> getDefaultAvatarImage() {
        try {
            Resource resource = new ClassPathResource("static/images/default-avatar.png");
            byte[] image = Files.readAllBytes(resource.getFile().toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
