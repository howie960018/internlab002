package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseStatus;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseBeanServiceSearchTest {

    @Mock
    private CourseBeanRepository repo;

    @InjectMocks
    private CourseBeanServiceJPAImplement service;

    @Test
    public void testSearchDelegatesToSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CourseBean> page = new PageImpl<>(List.of(), pageable, 0);
        when(repo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<CourseBean> result = service.search("java", 1L, CourseStatus.PUBLISHED, 10.0, 99.0, pageable);

        assertThat(result).isSameAs(page);
        verify(repo).findAll(any(Specification.class), eq(pageable));
    }
}
