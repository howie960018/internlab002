package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.bean.CartItem;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.Order;
import com.ctbc.assignment2.bean.OrderItem;
import com.ctbc.assignment2.bean.OrderStatus;
import com.ctbc.assignment2.exception.DuplicateEnrollmentException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.AppUserRepository;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.OrderItemRepository;
import com.ctbc.assignment2.repository.OrderRepository;
import com.ctbc.assignment2.service.EnrollmentService;
import com.ctbc.assignment2.service.impl.OrderServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private CourseBeanRepository courseRepository;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    public void testCreateOrderSuccess() {
        AppUser buyer = new AppUser();
        buyer.setUsername("user1");

        CourseBean course = new CourseBean();
        course.setId(1L);
        course.setCourseName("Course A");

        List<CartItem> items = List.of(new CartItem(1L, "Course A", 150.0));

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(buyer));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder("user1", items);

        assertThat(order.getBuyer()).isSameAs(buyer);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualTo(150.0);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getCourse()).isSameAs(course);
    }

    @Test
    public void testCreateOrderUserNotFound() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder("user1", List.of()));
    }

    @Test
    public void testPayOrderEnrollsCourses() {
        Order order = buildOrder("user1", OrderStatus.PENDING, 1L, 2L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = orderService.pay(1L, "user1");

        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(enrollmentService).enroll("user1", 1L);
        verify(enrollmentService).enroll("user1", 2L);
    }

    @Test
    public void testPayOrderIgnoresDuplicateEnrollment() {
        Order order = buildOrder("user1", OrderStatus.PENDING, 1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new DuplicateEnrollmentException("Already enrolled"))
                .when(enrollmentService).enroll("user1", 1L);

        Order saved = orderService.pay(1L, "user1");

        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    public void testCancelOrder() {
        Order order = buildOrder("user1", OrderStatus.PENDING, 1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancel(1L, "user1");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    public void testPayOrderNotOwnerThrows() {
        Order order = buildOrder("owner", OrderStatus.PENDING, 1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.pay(1L, "user1"));
    }

    private Order buildOrder(String username, OrderStatus status, Long... courseIds) {
        AppUser buyer = new AppUser();
        buyer.setUsername(username);

        Order order = new Order();
        order.setId(1L);
        order.setBuyer(buyer);
        order.setStatus(status);

        double total = 0.0;
        for (Long courseId : courseIds) {
            CourseBean course = new CourseBean();
            course.setId(courseId);
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setCourse(course);
            item.setPrice(100.0);
            order.getItems().add(item);
            total += 100.0;
        }
        order.setTotalAmount(total);
        return order;
    }
}
