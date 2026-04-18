package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CartItem;
import com.ctbc.assignment2.bean.Order;
import com.ctbc.assignment2.controller.web.OrderWebController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.CartService;
import com.ctbc.assignment2.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {OrderWebController.class})
@AutoConfigureMockMvc(addFilters = false)
public class OrderWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testListOrders() throws Exception {
        when(orderService.findByBuyer("user1")).thenReturn(List.of(new Order()));

        mockMvc.perform(get("/orders")
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/index"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    public void testCreateOrderEmptyCartRedirects() throws Exception {
        when(cartService.getCart(any())).thenReturn(List.of());

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    public void testCreateOrderRedirectsToDetail() throws Exception {
        List<CartItem> items = List.of(new CartItem(1L, "Course", 120.0));
        Order order = new Order();
        order.setId(5L);

        when(cartService.getCart(any())).thenReturn(items);
        when(orderService.createOrder(anyString(), any())).thenReturn(order);

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/5"));

        verify(cartService).clearCart(any());
    }

    @Test
    public void testOrderDetail() throws Exception {
        Order order = new Order();
        order.setId(3L);
        when(orderService.findById(3L)).thenReturn(order);

        mockMvc.perform(get("/orders/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    public void testPayOrder() throws Exception {
        mockMvc.perform(post("/orders/7/pay")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/7"));

        verify(orderService).pay(7L, "user1");
    }

    @Test
    public void testCancelOrder() throws Exception {
        mockMvc.perform(post("/orders/7/cancel")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).cancel(7L, "user1");
    }
}
