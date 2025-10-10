package com.waiterpos.entities;

import com.google.gson.annotations.SerializedName;

public class Orders {
    @SerializedName("ORDER_ID")
    private String orderID;
    @SerializedName("SUBTOTAL")
    private String subtotal;
    @SerializedName("STATUS")
    private String status;
    @SerializedName("TYPE")
    private String type;
    @SerializedName("ORDER_DATE")
    private String orderDate;

    public String getOrderID() {
        return orderID;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getOrderDate() {
        return orderDate;
    }
}
