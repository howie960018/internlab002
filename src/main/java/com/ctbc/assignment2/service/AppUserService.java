package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public AppUser registerUser(String username, String rawPassword) {
        return createUser(username, rawPassword, "USER");
    }

    public AppUser registerInstructor(String username, String rawPassword) {
        return createUser(username, rawPassword, "INSTRUCTOR");
    }

    public AppUser createUser(String username, String rawPassword, String role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    public AppUser findByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public boolean changePassword(String username, String currentRaw, String newRaw) {
        AppUser user = findByUsername(username);
        if (!passwordEncoder.matches(currentRaw, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newRaw));
        userRepository.save(user);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = findByUsername(username);
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
