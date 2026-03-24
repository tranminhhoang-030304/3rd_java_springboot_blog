package com.java_springboot_3rd.user_core;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TrackingEvent implements Serializable {
    private String userId;
    private String action; // Ví dụ: "CLICK_ADD_TO_CART", "LOGIN", "MOVE_FORWARD"
    private String platform; // "WEB", "ANDROID", "IOS"
    private String timestamp;

    public TrackingEvent() {}

    public TrackingEvent(String userId, String action, String platform) {
        this.userId = userId;
        this.action = action;
        this.platform = platform;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}