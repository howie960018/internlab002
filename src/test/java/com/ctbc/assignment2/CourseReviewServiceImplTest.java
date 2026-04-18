package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseReview;
import com.ctbc.assignment2.exception.DuplicateEnrollmentException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.AppUserRepository;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseReviewRepository;
import com.ctbc.assignment2.repository.EnrollmentRepository;
import com.ctbc.assignment2.service.impl.CourseReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseReviewServiceImplTest {

    @Mock
    private CourseReviewRepository reviewRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private CourseBeanRepository courseRepository;

    @InjectMocks
    private CourseReviewServiceImpl reviewService;

    @Test
    public void testSubmitRequiresEnrollment() {
        when(enrollmentRepository.existsByStudentUsernameAndCourseId("user1", 1L)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> reviewService.submit("user1", 1L, 5, "Great"));
    }

    @Test
    public void testSubmitDuplicateReview() {
        when(enrollmentRepository.existsByStudentUsernameAndCourseId("user1", 1L)).thenReturn(true);
        when(reviewRepository.existsByCourseIdAndReviewerUsername(1L, "user1")).thenReturn(true);

        assertThrows(DuplicateEnrollmentException.class,
                () -> reviewService.submit("user1", 1L, 4, "Nice"));
    }

    @Test
    public void testSubmitSuccess() {
        AppUser reviewer = new AppUser();
        reviewer.setUsername("user1");
        CourseBean course = new CourseBean();
        course.setId(1L);

        when(enrollmentRepository.existsByStudentUsernameAndCourseId("user1", 1L)).thenReturn(true);
        when(reviewRepository.existsByCourseIdAndReviewerUsername(1L, "user1")).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(reviewer));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(reviewRepository.save(any(CourseReview.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseReview saved = reviewService.submit("user1", 1L, 5, "Great");

        assertThat(saved.getReviewer()).isSameAs(reviewer);
        assertThat(saved.getCourse()).isSameAs(course);
        assertThat(saved.getRating()).isEqualTo(5);
    }

    @Test
    public void testDeleteNotFound() {
        when(reviewRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.delete(9L));
    }

    @Test
    public void testDeleteSuccess() {
        CourseReview review = new CourseReview();
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        reviewService.delete(3L);

        verify(reviewRepository).delete(review);
    }

    @Test
    public void testGetAvgRatingDefaultsToZero() {
        when(reviewRepository.findAvgRatingByCourseId(1L)).thenReturn(null);

        Double avg = reviewService.getAvgRating(1L);

        assertThat(avg).isEqualTo(0.0);
    }

    @Test
    public void testFindByCourse() {
        when(reviewRepository.findByCourseIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(new CourseReview()));

        List<CourseReview> reviews = reviewService.findByCourse(2L);

        assertThat(reviews).hasSize(1);
    }
}
