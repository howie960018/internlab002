package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileWebController {

    @Autowired
    private AppUserService appUserService;

    @GetMapping
    public String profile(Principal principal, Model model) {
        AppUser user = appUserService.findByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("enrollmentCount", 0);
        return "profile/index";
    }

    @PostMapping("/change-password")
    public String changePassword(Principal principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return "redirect:/profile?error=invalid";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/profile?error=invalid";
        }
        boolean changed = appUserService.changePassword(principal.getName(), currentPassword, newPassword);
        if (!changed) {
            return "redirect:/profile?error=current";
        }
        return "redirect:/profile?success";
    }
}
