package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.CategoryHierarchyException;
import com.ctbc.assignment2.exception.CategoryNotEmptyException;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import com.ctbc.assignment2.service.impl.CourseCategoryBeanServiceJPAImplement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseCategoryBeanServiceImplTest {

    @Mock
    private CourseCategoryBeanRepository repo;

    @Mock
    private CourseBeanRepository courseRepo;

    @InjectMocks
    private CourseCategoryBeanServiceJPAImplement service;

    @Test
    public void testSaveParentIsSelfThrows() {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setId(1L);
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(1L);
        category.setParent(parent);

        assertThrows(CategoryHierarchyException.class, () -> service.save(category));
    }

    @Test
    public void testSaveParentNotFoundThrows() {
        CourseCategoryBean category = new CourseCategoryBean();
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(2L);
        category.setParent(parent);

        when(repo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.save(category));
    }

    @Test
    public void testSaveParentHasParentThrows() {
        CourseCategoryBean category = new CourseCategoryBean();
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(2L);
        CourseCategoryBean grand = new CourseCategoryBean();
        grand.setId(3L);
        parent.setParent(grand);
        category.setParent(parent);

        when(repo.findById(2L)).thenReturn(Optional.of(parent));

        assertThrows(CategoryHierarchyException.class, () -> service.save(category));
    }

    @Test
    public void testSaveUpdateDuplicateNameThrows() {
        CourseCategoryBean incoming = new CourseCategoryBean();
        incoming.setId(5L);
        incoming.setCategoryName("Dup");

        CourseCategoryBean existing = new CourseCategoryBean();
        existing.setId(5L);
        existing.setCategoryName("Old");

        when(repo.findById(5L)).thenReturn(Optional.of(existing));
        when(repo.existsByCategoryNameAndIdNot("Dup", 5L)).thenReturn(true);

        assertThrows(DuplicateCourseNameException.class, () -> service.save(incoming));
    }

    @Test
    public void testSaveUpdateParentHasChildrenThrows() {
        CourseCategoryBean incoming = new CourseCategoryBean();
        incoming.setId(7L);
        incoming.setCategoryName("Name");
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(8L);
        incoming.setParent(parent);

        CourseCategoryBean existing = new CourseCategoryBean();
        existing.setId(7L);
        existing.setCategoryName("Name");

        when(repo.findById(8L)).thenReturn(Optional.of(parent));
        when(repo.findById(7L)).thenReturn(Optional.of(existing));
        when(repo.existsByParentId(7L)).thenReturn(true);

        assertThrows(CategoryHierarchyException.class, () -> service.save(incoming));
    }

    @Test
    public void testSaveNewDuplicateNameThrows() {
        CourseCategoryBean incoming = new CourseCategoryBean();
        incoming.setCategoryName("DupNew");

        when(repo.existsByCategoryName("DupNew")).thenReturn(true);

        assertThrows(DuplicateCourseNameException.class, () -> service.save(incoming));
    }

    @Test
    public void testDeleteByIdNotExistsThrows() {
        when(repo.existsById(9L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteById(9L));
    }

    @Test
    public void testDeleteByIdHasChildrenThrows() {
        when(repo.existsById(10L)).thenReturn(true);
        when(repo.existsByParentId(10L)).thenReturn(true);

        assertThrows(CategoryNotEmptyException.class, () -> service.deleteById(10L));
    }

    @Test
    public void testDeleteByIdHasCoursesThrows() {
        when(repo.existsById(11L)).thenReturn(true);
        when(repo.existsByParentId(11L)).thenReturn(false);
        when(courseRepo.existsByCategoryId(11L)).thenReturn(true);

        assertThrows(CategoryNotEmptyException.class, () -> service.deleteById(11L));
    }

    @Test
    public void testDeleteByIdSuccess() {
        when(repo.existsById(12L)).thenReturn(true);
        when(repo.existsByParentId(12L)).thenReturn(false);
        when(courseRepo.existsByCategoryId(12L)).thenReturn(false);

        service.deleteById(12L);

        verify(repo).deleteById(12L);
    }

    @Test
    public void testSaveUpdateSuccess() {
        CourseCategoryBean incoming = new CourseCategoryBean();
        incoming.setId(15L);
        incoming.setCategoryName("Updated");

        CourseCategoryBean existing = new CourseCategoryBean();
        existing.setId(15L);
        existing.setCategoryName("Old");

        when(repo.findById(15L)).thenReturn(Optional.of(existing));
        when(repo.existsByCategoryNameAndIdNot("Updated", 15L)).thenReturn(false);
        when(repo.save(existing)).thenReturn(existing);

        CourseCategoryBean result = service.save(incoming);

        assertThat(result.getCategoryName()).isEqualTo("Updated");
    }
}
