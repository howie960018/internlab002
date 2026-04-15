package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.controller.rest.CategoryBeanRestController;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CategoryBeanRestController.class})
public class CategoryBeanRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @Test
    public void testGetAllHappyPath() throws Exception {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setId(1L);
        category.setCategoryName("類別A");

        when(categoryService.findAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/category/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("類別A"));

        System.out.println("✅ testGetAllHappyPath 通過");
    }

    @Test
    public void testGetByIdHappyPath() throws Exception {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setId(1L);
        category.setCategoryName("類別A");

        when(categoryService.findById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("類別A"));

        System.out.println("✅ testGetByIdHappyPath 通過");
    }

    @Test
    public void testDeleteHappyPath() throws Exception {
        doNothing().when(categoryService).deleteById(1L);

        mockMvc.perform(delete("/api/category/1"))
                .andExpect(status().isOk());

        System.out.println("✅ testDeleteHappyPath 通過");
    }

    @Test
    public void testSaveHappyPath() throws Exception {
        CourseCategoryBean saved = new CourseCategoryBean();
        saved.setId(1L);
        saved.setCategoryName("類別A");

        when(categoryService.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"類別A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("類別A"));

        System.out.println("✅ testSaveHappyPath 通過");
    }
}
