package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.Chapter;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.ChapterRepository;
import com.ctbc.assignment2.service.impl.ChapterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChapterServiceImplTest {

    @Mock
    private ChapterRepository repository;

    @InjectMocks
    private ChapterServiceImpl service;

    @Test
    public void testFindByCourse() {
        when(repository.findByCourseIdOrderBySortOrder(1L)).thenReturn(List.of(new Chapter()));

        List<Chapter> result = service.findByCourse(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    public void testFindByIdNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    public void testDeleteNotFound() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
    }

    @Test
    public void testDeleteSuccess() {
        when(repository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repository).deleteById(5L);
    }
}
