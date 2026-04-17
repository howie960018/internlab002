package com.ctbc.assignment2.config;

import com.ctbc.assignment2.service.AppUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppUserSeeder implements CommandLineRunner {

    private final AppUserService appUserService;

    public AppUserSeeder(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public void run(String... args) {
        if (!appUserService.existsByUsername("admin")) {
            appUserService.createUser("admin", "admin123", "ADMIN");
        }
        if (!appUserService.existsByUsername("user")) {
            appUserService.createUser("user", "user123", "USER");
        }
    }
}
