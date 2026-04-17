package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.controller.web.RegistrationWebController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {RegistrationWebController.class})
@AutoConfigureMockMvc(addFilters = false)
public class RegistrationWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserService appUserService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testShowRegisterForm() throws Exception {
        mockMvc.perform(get("/register").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        when(appUserService.existsByUsername("newuser")).thenReturn(false);
        when(appUserService.registerUser("newuser", "password123")).thenReturn(new AppUser());

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "newuser")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    public void testRegisterPasswordMismatch() throws Exception {
        when(appUserService.existsByUsername("newuser")).thenReturn(false);

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "newuser")
                .param("password", "password123")
                .param("confirmPassword", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "confirmPassword"));
    }

    @Test
    public void testRegisterDuplicateUsername() throws Exception {
        when(appUserService.existsByUsername("newuser")).thenReturn(true);

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "newuser")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "username"));
    }
}
