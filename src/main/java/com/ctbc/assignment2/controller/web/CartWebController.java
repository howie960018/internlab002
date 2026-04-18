package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CartItem;
import com.ctbc.assignment2.bean.Order;
import com.ctbc.assignment2.service.CartService;
import com.ctbc.assignment2.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartWebController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = cartService.getCart(session);
        model.addAttribute("items", cart);
        model.addAttribute("total", cartService.getTotal(session));
        return "cart/index";
    }

    @PostMapping("/add/{courseId}")
    public String addToCart(@PathVariable Long courseId, HttpSession session) {
        cartService.addToCart(session, courseId);
        return "redirect:/courses/" + courseId;
    }

    @PostMapping("/remove/{courseId}")
    public String removeFromCart(@PathVariable Long courseId, HttpSession session) {
        cartService.removeFromCart(session, courseId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, Principal principal) {
        List<CartItem> items = cartService.getCart(session);
        if (items == null || items.isEmpty()) {
            return "redirect:/cart";
        }
        Order order = orderService.createOrder(principal.getName(), items);
        cartService.clearCart(session);
        return "redirect:/orders/" + order.getId();
    }
}
