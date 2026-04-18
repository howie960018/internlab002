package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.bean.Enrollment;
import com.ctbc.assignment2.repository.AppUserRepository;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseCategoryBeanRepository;
import com.ctbc.assignment2.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private CourseBeanRepository courseRepository;

    @Autowired
    private CourseCategoryBeanRepository categoryRepository;

    @Test
    public void testEnrollmentQueries() {
        AppUser user = new AppUser();
        user.setUsername("student1");
        user.setPassword("pass");
        user.setRole("USER");
        userRepository.save(user);

        CourseCategoryBean category = new CourseCategoryBean();
        category.setCategoryName("Test category");
        categoryRepository.save(category);

        CourseBean course = new CourseBean();
        course.setCourseName("Test course");
        course.setPrice(100.0);
        course.setCategory(category);
        courseRepository.save(course);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(user);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);

        em.flush();
        em.clear();

        assertThat(enrollmentRepository.existsByStudentUsernameAndCourseId("student1", course.getId())).isTrue();
        assertThat(enrollmentRepository.findByStudentUsername("student1")).hasSize(1);
        assertThat(enrollmentRepository.countByCourseId(course.getId())).isEqualTo(1);
    }
}
