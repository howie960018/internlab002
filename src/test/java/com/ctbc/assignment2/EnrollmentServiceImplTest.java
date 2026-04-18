package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.Enrollment;
import com.ctbc.assignment2.exception.DuplicateEnrollmentException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.AppUserRepository;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.EnrollmentRepository;
import com.ctbc.assignment2.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private CourseBeanRepository courseRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    public void testEnrollDuplicateThrows() {
        when(enrollmentRepository.existsByStudentUsernameAndCourseId("user1", 1L)).thenReturn(true);

        assertThrows(DuplicateEnrollmentException.class, () -> enrollmentService.enroll("user1", 1L));
    }

    @Test
    public void testEnrollUserNotFoundThrows() {
        when(enrollmentRepository.existsByStudentUsernameAndCourseId("user1", 1L)).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.enroll("user1", 1L));
    }

    @Test
    public void testEnrollCourseNotFoundThrows() {
        when(enrollmentRepository.existsByStudentUsernameAndCourseId("user1", 1L)).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new AppUser()));
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.enroll("user1", 1L));
    }

    @Test
    public void testEnrollSuccess() {
        AppUser user = new AppUser();
        user.setUsername("user1");
        CourseBean course = new CourseBean();
        course.setId(1L);

        when(enrollmentRepository.existsByStudentUsernameAndCourseId("user1", 1L)).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        enrollmentService.enroll("user1", 1L);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        Enrollment saved = captor.getValue();
        assertThat(saved.getStudent()).isSameAs(user);
        assertThat(saved.getCourse()).isSameAs(course);
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    public void testFindByStudent() {
        when(enrollmentRepository.findByStudentUsername("user1"))
                .thenReturn(List.of(new Enrollment()));

        List<Enrollment> result = enrollmentService.findByStudent("user1");

        assertThat(result).hasSize(1);
    }

    @Test
    public void testCountByCourse() {
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(5L);

        long count = enrollmentService.countByCourse(1L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    public void testCancelUpdatesStatus() {
        Enrollment enrollment = new Enrollment();
        enrollment.setStatus("ACTIVE");
        when(enrollmentRepository.findByStudentUsernameAndCourseId("user1", 1L))
                .thenReturn(Optional.of(enrollment));

        enrollmentService.cancel("user1", 1L);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("CANCELLED");
    }
}
