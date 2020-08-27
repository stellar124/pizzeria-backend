package com.codecool.pizzabackend.service;

import com.codecool.pizzabackend.controller.dto.OrderrDTO;
import com.codecool.pizzabackend.controller.dto.PizzaQuantityDTO;
import com.codecool.pizzabackend.entity.*;
import com.codecool.pizzabackend.repository.OrderrRepository;
import com.codecool.pizzabackend.repository.PizzaRepository;
import com.codecool.pizzabackend.repository.UserRepository;
import net.bytebuddy.implementation.bytecode.Throw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private OrderrRepository orderrRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    public void persistIncomingOrder(Long userId, OrderrDTO orderrDTO) {
        LOGGER.trace("Starting to create incoming order from userId: " + userId + "incomingOrderDTO: " + orderrDTO.toString());
        HashMap<Pizza, Integer> pizzas = new HashMap<>();
        for (PizzaQuantityDTO orderedPizzaQuantity : orderrDTO.getIncomingOrderedPizzas()) {
            pizzas.put(pizzaRepository.getPizzaById(orderedPizzaQuantity.getId()), orderedPizzaQuantity.getQuantity());
        }
        User customer = userRepository.getUserById(userId);
        Orderr orderr = Orderr.builder()
                .customer((Customer) customer)
                .orderStatus(OrderStatus.ORDERED)
                .orderedPizzas(pizzas)
                .build();

        LOGGER.info("Incoming order created. incoming order: " + orderr.toString());
        orderrRepository.save(orderr);
        LOGGER.info("Incoming order persisted to db. incoming order: " + orderr.toString());
    }

    private List<OrderrDTO> generateIncomingOrderDTOsFromOrders(List<Orderr> orders) {
        List<OrderrDTO> orderDTOs = new ArrayList<>();
        for (Orderr order : orders) {
            orderDTOs.add(order.generateIncomingOrderDTO());
        }
        return orderDTOs;
    }

    public List<OrderrDTO> listActiveOrdersForUser(Long userId) {
        LOGGER.info("listActiveOrdersForUser started");
        //TODO: Maybe do the the whole thing in one sql.
        //TODO: error handling!
        User user = userRepository.getUserById(userId);
        String userRole = user.getRoles().stream().findFirst().get();
        LOGGER.info(" User role queired for userid: " + userId + " found role: " + userRole);
        List<Orderr> activeOrders = new ArrayList<>();
        switch (userRole) {
            case "ROLE_CUSTOMER": {
                LOGGER.info("Starting to list orders for customer. user id: " + userId);
                activeOrders = orderrRepository.getOrderrsByOrderStatusNotLikeAndCustomer_IdIs(OrderStatus.DELIVERED, userId);
                LOGGER.info(String.format("active orders are: %s",activeOrders.toString()));
                break;
            }
            case "ROLE_MANAGER": {
                LOGGER.info("Starting to list orders for manager. user id: " + userId);
                activeOrders = orderrRepository.getOrderrsByOrderStatusNotLike(OrderStatus.DELIVERED);
                break;
            }
            case "ROLE_COOK": {
                LOGGER.info("Starting to list orders for cook. user id: " + userId);
                activeOrders = orderrRepository.getCookActiveAssignedOrders(userId);
                break;
            }
            case "ROLE_DELIVERYGUY": {
                LOGGER.info("Starting to list orders for deliveryGuy. user id: " + userId);
                activeOrders = orderrRepository.getDeliveryGuyActiveAssignedOrders(userId);
                break;
            }
        }
        List<OrderrDTO> activeOrdersDTOs = generateIncomingOrderDTOsFromOrders(activeOrders);
        return activeOrdersDTOs;
    }

    private boolean isCookOwnsOrder(Long orderId, Long cookId){
        LOGGER.info(String.format("Started to check if order with id: %s is owned by user: %s", orderId,cookId));
        boolean result = orderrRepository.isOrderOwnedByCook(orderId,cookId);
        LOGGER.info(String.format("Finished to check if order with id: %s is owned by user: %s result is: %s", orderId,cookId,result));
        return result;
    }

    public void setOrderStatusToReady(Long orderId, Long cookId) throws OrederrNotFoundException {
        LOGGER.info(String.format("Started the process of updating order status to READY order id: %s is  cook id: %s", orderId,cookId));
        if(isCookOwnsOrder(orderId, cookId)){
            Orderr orderr = orderrRepository.findById(orderId)
                    .orElseThrow(() -> new OrederrNotFoundException(String.format("Order with id: %s not found",orderId)));
            orderr.setOrderStatus(OrderStatus.READY);
            orderrRepository.save(orderr);
        }
        LOGGER.info(String.format("Finished the process of updating order status to READY order id: %s is  cook id: %s", orderId,cookId));
    }
}




