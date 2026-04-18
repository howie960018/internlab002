package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CartItem;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseStatus;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.impl.CartServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CourseBeanService courseService;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    public void testGetCartCreatesList() {
        HttpSession session = new MockHttpSession();

        List<CartItem> cart = cartService.getCart(session);

        assertThat(cart).isNotNull();
        assertThat(cart).isEmpty();
        assertThat(session.getAttribute("CART")).isSameAs(cart);
    }

    @Test
    public void testAddToCartAddsPublishedCourse() {
        HttpSession session = new MockHttpSession();
        CourseBean course = new CourseBean();
        course.setId(1L);
        course.setCourseName("Java");
        course.setPrice(100.0);
        course.setStatus(CourseStatus.PUBLISHED);
        when(courseService.findById(1L)).thenReturn(course);

        cartService.addToCart(session, 1L);

        List<CartItem> cart = cartService.getCart(session);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getCourseId()).isEqualTo(1L);
        assertThat(cart.get(0).getPrice()).isEqualTo(100.0);
    }

    @Test
    public void testAddToCartIgnoresDuplicates() {
        HttpSession session = new MockHttpSession();
        CourseBean course = new CourseBean();
        course.setId(2L);
        course.setCourseName("Spring");
        course.setPrice(200.0);
        course.setStatus(CourseStatus.PUBLISHED);
        when(courseService.findById(2L)).thenReturn(course);

        cartService.addToCart(session, 2L);
        cartService.addToCart(session, 2L);

        assertThat(cartService.getCart(session)).hasSize(1);
    }

    @Test
    public void testAddToCartRejectsUnpublishedCourse() {
        HttpSession session = new MockHttpSession();
        CourseBean course = new CourseBean();
        course.setId(3L);
        course.setCourseName("Draft course");
        course.setPrice(50.0);
        course.setStatus(CourseStatus.DRAFT);
        when(courseService.findById(3L)).thenReturn(course);

        assertThrows(IllegalStateException.class, () -> cartService.addToCart(session, 3L));
    }

    @Test
    public void testRemoveFromCart() {
        HttpSession session = new MockHttpSession();
        CourseBean course = new CourseBean();
        course.setId(4L);
        course.setCourseName("Remove me");
        course.setPrice(120.0);
        course.setStatus(CourseStatus.PUBLISHED);
        when(courseService.findById(4L)).thenReturn(course);

        cartService.addToCart(session, 4L);
        cartService.removeFromCart(session, 4L);

        assertThat(cartService.getCart(session)).isEmpty();
    }

    @Test
    public void testClearCart() {
        HttpSession session = new MockHttpSession();
        CourseBean course = new CourseBean();
        course.setId(5L);
        course.setCourseName("Clear me");
        course.setPrice(150.0);
        course.setStatus(CourseStatus.PUBLISHED);
        when(courseService.findById(5L)).thenReturn(course);

        cartService.addToCart(session, 5L);
        cartService.clearCart(session);

        assertThat(cartService.getCart(session)).isEmpty();
    }

    @Test
    public void testGetTotalIgnoresNullPrice() {
        HttpSession session = new MockHttpSession();
        List<CartItem> cart = cartService.getCart(session);
        cart.add(new CartItem(10L, "A", 100.0));
        cart.add(new CartItem(11L, "B", null));
        cart.add(new CartItem(12L, "C", 50.0));

        double total = cartService.getTotal(session);

        assertThat(total).isEqualTo(150.0);
    }
}
