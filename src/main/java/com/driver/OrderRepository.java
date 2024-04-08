// OrderRepository.java
package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private HashMap<String, Order> orderMap;
    private HashMap<String, DeliveryPartner> partnerMap;
    private HashMap<String, HashSet<String>> partnerToOrderMap;
    private HashMap<String, String> orderToPartnerMap;

    public OrderRepository(){
        this.orderMap = new HashMap<String, Order>();
        this.partnerMap = new HashMap<String, DeliveryPartner>();
        this.partnerToOrderMap = new HashMap<String, HashSet<String>>();
        this.orderToPartnerMap = new HashMap<String, String>();
    }

    public void saveOrder(Order order){
        orderMap.put(order.getId(), order);
    }

    public void savePartner(String partnerId){
        partnerMap.put(partnerId, new DeliveryPartner(partnerId));
    }

    public void saveOrderPartnerMap(String orderId, String partnerId){
        if(orderMap.containsKey(orderId) && partnerMap.containsKey(partnerId)){
            orderToPartnerMap.put(orderId, partnerId);
            partnerToOrderMap.computeIfAbsent(partnerId, k -> new HashSet<>()).add(orderId);
            partnerMap.get(partnerId).incrementNumberOfOrders();
        }
    }

    public Order findOrderById(String orderId){
        return orderMap.get(orderId);
    }

    public DeliveryPartner findPartnerById(String partnerId){
        return partnerMap.get(partnerId);
    }

    public Integer findOrderCountByPartnerId(String partnerId){
        return partnerToOrderMap.getOrDefault(partnerId, new HashSet<>()).size();
    }

    public List<String> findOrdersByPartnerId(String partnerId){
        return new ArrayList<>(partnerToOrderMap.getOrDefault(partnerId, new HashSet<>()));
    }

    public List<String> findAllOrders(){
        return new ArrayList<>(orderMap.keySet());
    }

    public void deletePartner(String partnerId){
        if (partnerMap.containsKey(partnerId)) {
            HashSet<String> orders = partnerToOrderMap.remove(partnerId);
            for (String orderId : orders) {
                orderToPartnerMap.remove(orderId);
            }
            partnerMap.remove(partnerId);
        }
    }

    public void deleteOrder(String orderId){
        String partnerId = orderToPartnerMap.remove(orderId);
        if (partnerId != null) {
            partnerToOrderMap.get(partnerId).remove(orderId);
            partnerMap.get(partnerId).decrementNumberOfOrders();
        }
        orderMap.remove(orderId);
    }

    public Integer findCountOfUnassignedOrders(){
        return (int) orderToPartnerMap.values().stream().filter(partnerId -> partnerId == null).count();
    }

    public Integer findOrdersLeftAfterGivenTimeByPartnerId(String timeString, String partnerId){
        int time = Integer.parseInt(timeString.split(":")[0]) * 60 + Integer.parseInt(timeString.split(":")[1]);
        int count = 0;
        if (partnerToOrderMap.containsKey(partnerId)) {
            for (String orderId : partnerToOrderMap.get(partnerId)) {
                Order order = orderMap.get(orderId);
                if (order.getDeliveryTime() > time) {
                    count++;
                }
            }
        }
        return count;
    }

    public String findLastDeliveryTimeByPartnerId(String partnerId){
        int lastDeliveryTime = Integer.MIN_VALUE;
        if (partnerToOrderMap.containsKey(partnerId)) {
            for (String orderId : partnerToOrderMap.get(partnerId)) {
                Order order = orderMap.get(orderId);
                lastDeliveryTime = Math.max(lastDeliveryTime, order.getDeliveryTime());
            }
        }
        int hours = lastDeliveryTime / 60;
        int minutes = lastDeliveryTime % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
