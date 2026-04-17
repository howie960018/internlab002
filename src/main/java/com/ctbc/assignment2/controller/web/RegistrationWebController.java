package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.service.AppUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationWebController {

    private final AppUserService appUserService;

    public RegistrationWebController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form,
                           BindingResult bindingResult) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }
        if (appUserService.existsByUsername(form.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Username already exists");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        appUserService.registerUser(form.getUsername(), form.getPassword());
        return "redirect:/login?registered";
    }

    public static class RegisterForm {
        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 40, message = "Username must be 4 to 40 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 60, message = "Password must be 6 to 60 characters")
        private String password;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}
