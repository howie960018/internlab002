package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CartItem;
import com.ctbc.assignment2.controller.web.CartWebController;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(controllers = {CartWebController.class, OrderWebController.class})
@AutoConfigureMockMvc(addFilters = false)
public class CartWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testViewCart() throws Exception {
        List<CartItem> items = List.of(new CartItem(1L, "Course A", 100.0));
        when(cartService.getCart(any())).thenReturn(items);
        when(cartService.getTotal(any())).thenReturn(100.0);

        mockMvc.perform(get("/cart").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/index"))
                .andExpect(model().attribute("items", items))
                .andExpect(model().attribute("total", 100.0));
    }

    @Test
    public void testAddToCart() throws Exception {
        mockMvc.perform(post("/cart/add/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"));

        verify(cartService).addToCart(any(), any());
    }

    @Test
    public void testRemoveFromCart() throws Exception {
        mockMvc.perform(post("/cart/remove/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeFromCart(any(), any());
    }

    @Test
    public void testClearCart() throws Exception {
        mockMvc.perform(post("/cart/clear").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).clearCart(any());
    }

    @Test
    public void testCheckout() throws Exception {
        List<CartItem> items = List.of(
                new CartItem(1L, "Course A", 100.0),
                new CartItem(2L, "Course B", 200.0)
        );
        when(cartService.getCart(any())).thenReturn(items);
        com.ctbc.assignment2.bean.Order order = new com.ctbc.assignment2.bean.Order();
        order.setId(5L);
        when(orderService.createOrder(anyString(), any())).thenReturn(order);

        mockMvc.perform(post("/cart/checkout")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/orders/5"));

        verify(orderService).createOrder(anyString(), any());
        verify(cartService).clearCart(any());
    }
}
