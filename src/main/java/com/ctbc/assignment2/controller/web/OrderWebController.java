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
@RequestMapping("/orders")
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("orders", orderService.findByBuyer(principal.getName()));
        return "orders/index";
    }

    @PostMapping("/create")
    public String create(HttpSession session, Principal principal) {
        List<CartItem> items = cartService.getCart(session);
        if (items == null || items.isEmpty()) {
            return "redirect:/cart";
        }
        Order order = orderService.createOrder(principal.getName(), items);
        cartService.clearCart(session);
        return "redirect:/orders/" + order.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        return "orders/detail";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, Principal principal) {
        orderService.pay(id, principal.getName());
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal) {
        orderService.cancel(id, principal.getName());
        return "redirect:/orders";
    }
}
