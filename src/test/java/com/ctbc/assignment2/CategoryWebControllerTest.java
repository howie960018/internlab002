package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.controller.web.CategoryWebController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
@AutoConfigureMockMvc(addFilters = false)
public class CategoryWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testListHappyPath() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/categories").with(csrf()))
                .andExpect(status().isOk())
            .andExpect(view().name("admin/category/list"))
                .andExpect(model().attributeExists("categories"));

        System.out.println("✅ testListHappyPath 通過");
    }

    @Test
    public void testShowFormHappyPath() throws Exception {
        when(categoryService.findTopLevel()).thenReturn(List.of());

        mockMvc.perform(get("/admin/category/form").with(csrf()))
                .andExpect(status().isOk())
            .andExpect(view().name("admin/category/form"))
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

        mockMvc.perform(get("/admin/category/edit/1").with(csrf()))
                .andExpect(status().isOk())
            .andExpect(view().name("admin/category/form"))
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

        mockMvc.perform(post("/admin/category/save")
                .with(csrf())
                .param("categoryName", "類別A"))
                .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/categories"));

        System.out.println("✅ testSaveHappyPath 通過");
    }

    @Test
    public void testDeleteHappyPath() throws Exception {
        doNothing().when(categoryService).deleteById(1L);

        mockMvc.perform(post("/admin/category/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
        System.out.println("✅ testDeleteHappyPath 通過");
    }
}
