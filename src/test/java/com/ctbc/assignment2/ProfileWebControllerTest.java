package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.controller.web.ProfileWebController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(controllers = {ProfileWebController.class})
@AutoConfigureMockMvc(addFilters = false)
public class ProfileWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserService appUserService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testProfileView() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("user1");
        user.setRole("USER");
        when(appUserService.findByUsername("user1")).thenReturn(user);

        mockMvc.perform(get("/profile")
                .with(csrf())
                .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/index"))
                .andExpect(model().attribute("username", "user1"))
                .andExpect(model().attribute("role", "USER"));
    }

    @Test
    public void testChangePasswordInvalidLength() throws Exception {
        mockMvc.perform(post("/profile/change-password")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a"))
                        .param("currentPassword", "oldpass")
                        .param("newPassword", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?error=invalid"));
    }

    @Test
    public void testChangePasswordMismatch() throws Exception {
        mockMvc.perform(post("/profile/change-password")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a"))
                        .param("currentPassword", "oldpass")
                        .param("newPassword", "123456")
                        .param("confirmPassword", "abcdef"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?error=invalid"));
    }

    @Test
    public void testChangePasswordWrongCurrent() throws Exception {
        when(appUserService.changePassword(anyString(), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/profile/change-password")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a"))
                        .param("currentPassword", "oldpass")
                        .param("newPassword", "123456")
                        .param("confirmPassword", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?error=current"));
    }

    @Test
    public void testChangePasswordSuccess() throws Exception {
        when(appUserService.changePassword(anyString(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/profile/change-password")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a"))
                        .param("currentPassword", "oldpass")
                        .param("newPassword", "123456")
                        .param("confirmPassword", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success"));
    }
}
