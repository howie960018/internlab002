package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseStatus;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.service.impl.CourseBeanServiceJPAImplement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseBeanServiceImplTest {

    @Mock
    private CourseBeanRepository repo;

    @InjectMocks
    private CourseBeanServiceJPAImplement service;

    @Test
    public void testDeleteByIdNotExistsThrows() {
        when(repo.existsById(9L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteById(9L));
    }

    @Test
    public void testUpdateStatusSaves() {
        CourseBean course = new CourseBean();
        course.setId(1L);
        course.setStatus(CourseStatus.DRAFT);
        when(repo.findById(1L)).thenReturn(Optional.of(course));
        when(repo.save(course)).thenReturn(course);

        CourseBean updated = service.updateStatus(1L, CourseStatus.PUBLISHED);

        assertThat(updated.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        verify(repo).save(course);
    }

    @Test
    public void testFindPageByCategoryIdsEmptyReturnsEmpty() {
        Page<CourseBean> page = service.findPageByCategoryIds(List.of(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void testFindPageByNameBlankReturnsEmpty() {
        Page<CourseBean> page = service.findPageByName("   ", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void testFindPageByCategoryIdsAndNameBlankUsesCategoryOnly() {
        Page<CourseBean> expected = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(repo.findByCategoryIdIn(eq(List.of(1L, 2L)), any())).thenReturn(expected);

        Page<CourseBean> result = service.findPageByCategoryIdsAndName(List.of(1L, 2L), " ", PageRequest.of(0, 10));

        assertThat(result).isSameAs(expected);
    }

    @Test
    public void testFindPublishedPageByNameBlankReturnsEmpty() {
        Page<CourseBean> page = service.findPublishedPageByName("", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void testFindByInstructorNameBlankReturnsEmpty() {
        assertThat(service.findByInstructorName(" ")).isEmpty();
    }

    @Test
    public void testFindByInstructorNameUsesTrimmed() {
        when(repo.findByInstructorName("amy")).thenReturn(List.of(new CourseBean()));

        List<CourseBean> result = service.findByInstructorName("  amy ");

        assertThat(result).hasSize(1);
    }

    @Test
    public void testSaveDuplicateNameThrows() {
        CourseBean existing = new CourseBean();
        existing.setId(1L);
        existing.setCourseName("Java");
        when(repo.findAll()).thenReturn(List.of(existing));

        CourseBean incoming = new CourseBean();
        incoming.setId(2L);
        incoming.setCourseName("Java");

        assertThrows(DuplicateCourseNameException.class, () -> service.save(incoming));
    }

    @Test
    public void testSaveBatchDuplicateInBatchThrows() {
        when(repo.findAll()).thenReturn(List.of());

        CourseBean c1 = new CourseBean();
        c1.setCourseName("BatchDup");
        CourseBean c2 = new CourseBean();
        c2.setCourseName("BatchDup");

        assertThrows(DuplicateCourseNameException.class, () -> service.saveBatch(List.of(c1, c2)));
    }

    @Test
    public void testFindByIdNotFoundThrows() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }
}
