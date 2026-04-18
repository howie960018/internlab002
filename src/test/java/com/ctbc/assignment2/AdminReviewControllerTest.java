package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseReview;
import com.ctbc.assignment2.controller.web.AdminReviewController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.CourseReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {AdminReviewController.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseReviewService reviewService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testListReviews() throws Exception {
        com.ctbc.assignment2.bean.AppUser reviewer = new com.ctbc.assignment2.bean.AppUser();
        reviewer.setUsername("user1");
        CourseReview review = new CourseReview();
        review.setReviewer(reviewer);
        when(reviewService.findByCourse(1L)).thenReturn(List.of(review));

        mockMvc.perform(get("/admin/course/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/review/list"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attribute("courseId", 1L));
    }

    @Test
    public void testDeleteReviewRedirects() throws Exception {
        mockMvc.perform(post("/admin/review/delete/3")
                        .param("courseId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/course/1/reviews"));

        verify(reviewService).delete(3L);
    }
}
