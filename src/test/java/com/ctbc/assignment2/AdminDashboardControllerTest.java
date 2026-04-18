package com.ctbc.assignment2;

import com.ctbc.assignment2.controller.web.AdminDashboardController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {AdminDashboardController.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testDashboardCounts() throws Exception {
        when(courseService.count()).thenReturn(12L);
        when(categoryService.count()).thenReturn(3L);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("courseCount", 12L))
                .andExpect(model().attribute("categoryCount", 3L));
    }
}
