package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.controller.web.CategoryWebController;
import com.ctbc.assignment2.security.SecurityConfig;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {CategoryWebController.class})
@Import({SecurityConfig.class, TestSecurityBeans.class})
@WithMockUser(roles = "ADMIN")
public class CategoryWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @MockBean
    private CourseBeanService courseService;

    @Test
    public void testListHappyPath() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/category/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/list"))
                .andExpect(model().attributeExists("categories"));

        System.out.println("✅ testListHappyPath 通過");
    }

    @Test
    public void testShowFormHappyPath() throws Exception {
        when(categoryService.findTopLevel()).thenReturn(List.of());

        mockMvc.perform(get("/category/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"))
            .andExpect(model().attributeExists("category"))
            .andExpect(model().attributeExists("parentOptions"));

        System.out.println("✅ testShowFormHappyPath 通過");
    }

    @Test
    public void testEditHappyPath() throws Exception {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setId(1L);
        category.setCategoryName("類別A");

        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(10L);
        parent.setCategoryName("主類別");
        category.setParent(parent);

        when(categoryService.findById(1L)).thenReturn(category);
        when(categoryService.findTopLevel()).thenReturn(List.of(parent));
        when(categoryService.findChildren(10L)).thenReturn(List.of());

        mockMvc.perform(get("/category/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"))
            .andExpect(model().attributeExists("category"))
            .andExpect(model().attributeExists("parentOptions"))
            .andExpect(model().attribute("selectedParentId", 10L));

        System.out.println("✅ testEditHappyPath 通過");
    }

    @Test
    public void testSaveHappyPath() throws Exception {
        CourseCategoryBean saved = new CourseCategoryBean();
        saved.setId(1L);
        saved.setCategoryName("類別A");

        when(categoryService.save(any())).thenReturn(saved);
        when(categoryService.findTopLevel()).thenReturn(List.of());

        mockMvc.perform(post("/category/save")
                .with(csrf())
                .param("categoryName", "類別A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/category/list"));

        System.out.println("✅ testSaveHappyPath 通過");
    }

    @Test
    public void testDeleteHappyPath() throws Exception {
        doNothing().when(categoryService).deleteById(1L);

        mockMvc.perform(post("/category/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/category/list"));

        System.out.println("✅ testDeleteHappyPath 通過");
    }

    @Test
    public void testBrowseHappyPath() throws Exception {
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(1L);
        parent.setCategoryName("主類別");

        CourseCategoryBean child = new CourseCategoryBean();
        child.setId(2L);
        child.setCategoryName("子類別");
        child.setParent(parent);

        when(categoryService.findTopLevel()).thenReturn(List.of(parent));
        when(categoryService.findChildren(1L)).thenReturn(List.of(child));
        when(categoryService.findById(1L)).thenReturn(parent);
        Page<com.ctbc.assignment2.bean.CourseBean> coursePage = new PageImpl<>(
            List.of(),
            PageRequest.of(0, 10),
            0
        );
        when(courseService.findPageByCategoryIds(any(), any())).thenReturn(coursePage);

        mockMvc.perform(get("/category/browse").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/browse"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("selectedCategory"))
            .andExpect(model().attributeExists("courses"))
            .andExpect(model().attribute("currentPage", 0))
            .andExpect(model().attribute("totalPages", 0))
            .andExpect(model().attribute("pageSize", 10));

        System.out.println("✅ testBrowseHappyPath 通過");
    }
}
