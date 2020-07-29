package com.codecool.pizzabackend.controller;

import com.codecool.pizzabackend.controller.dto.OrderrDTO;
import com.codecool.pizzabackend.entity.User;
import com.codecool.pizzabackend.repository.UserRepository;
import com.codecool.pizzabackend.security.JwtTokenServices;
import com.codecool.pizzabackend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/orders")
public class OrderrController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenServices jwtTokenServices;
    @Autowired
    private UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderrController.class);

    @GetMapping("/active")
    public List<OrderrDTO> getActiveOrdersForUser(HttpServletRequest request) {
        LOGGER.info("GET request /orders/active/ arrived");
        String username = jwtTokenServices.getTokenFromRequest(request);
        LOGGER.info(String.format("username from token: %s", username));
        User user = userRepository.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));
        List<OrderrDTO> activieOrderDTOs = orderService.listActiveOrdersForUser(user.getId());
        LOGGER.info(" Get request: /orders/active/ processed. Id was: " + user.getId() + " \n Return value will be: " + activieOrderDTOs.toString());
        return activieOrderDTOs;
    }

    @PostMapping("/add-new")
    public void createNewOrder(@RequestBody OrderrDTO orderrDTO, HttpServletRequest request) {
        LOGGER.info("post request: /orders/add-new arrived. payload: " + orderrDTO.toString());
        String username = jwtTokenServices.getTokenFromRequest(request);
        LOGGER.info(String.format("username from token: %s", username));
        User user = userRepository.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));
        orderService.persistIncomingOrder(user.getId(), orderrDTO);
        LOGGER.info("post request: /orders/add-new processed. Id was: " + user.getId());
    }


}
