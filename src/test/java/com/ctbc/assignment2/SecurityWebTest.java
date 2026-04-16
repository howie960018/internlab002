package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServerProperties serverProperties;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @Test
    public void unauthenticatedUserAccessCourseListRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/course/list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void userRoleCanAccessCourseListAndCategoryBrowse() throws Exception {
        stubCourseList();
        stubCategoryBrowse();

        mockMvc.perform(get("/course/list"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/category/browse"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void userRoleCannotAccessAdminOnlyCourseForm() throws Exception {
        mockMvc.perform(get("/course/form"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void adminRoleCanAccessAllCourseAndCategoryRoutes() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());
        when(categoryService.findTopLevel()).thenReturn(List.of());

        mockMvc.perform(get("/course/form"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/category/list"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void csrfIsRequiredForPostWebRoutes() throws Exception {
        mockMvc.perform(post("/course/save")
                        .param("courseName", "測試課程")
                        .param("price", "100.0"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteRoutesRequirePostAndCsrf() throws Exception {
        doNothing().when(courseService).deleteById(1L);

        mockMvc.perform(get("/course/delete/1"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(post("/course/delete/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/course/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void sessionTimeoutIsConfiguredForThirtyMinutes() {
        // 驗證 Session 逾時設定為 30 分鐘
        Duration timeout = serverProperties.getServlet().getSession().getTimeout();
        org.junit.jupiter.api.Assertions.assertEquals(Duration.ofMinutes(30), timeout);
    }

    private void stubCourseList() {
        Page<CourseBean> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(courseService.findPage(any(Pageable.class))).thenReturn(page);
    }

    private void stubCategoryBrowse() {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setId(1L);
        category.setCategoryName("主類別");

        when(categoryService.findTopLevel()).thenReturn(List.of(category));
        when(categoryService.findChildren(1L)).thenReturn(List.of());
        when(categoryService.findById(1L)).thenReturn(category);
        when(courseService.findPageByCategoryIds(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
    }
}
