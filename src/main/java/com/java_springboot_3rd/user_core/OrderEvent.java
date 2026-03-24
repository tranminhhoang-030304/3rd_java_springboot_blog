package com.java_springboot_3rd.user_core;

import java.io.Serializable;

public class OrderEvent implements Serializable {

    private String orderId;
    private String userEmail;
    private String productName;
    private double price;

    // Tạo HÀM TẠO RỖNG ĐỂ JACKSON CHUYỂN ĐỔI JSON ---
    public OrderEvent() {}

    public OrderEvent(String orderId, String userEmail, String productName, double price) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.productName = productName;
        this.price = price;
    }

    // --- GETTER & SETTER ---
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}