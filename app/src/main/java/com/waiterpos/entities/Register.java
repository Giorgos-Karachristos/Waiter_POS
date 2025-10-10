package com.waiterpos.entities;

import com.google.gson.annotations.SerializedName;

public class Register {
    @SerializedName("NAME")
    private String name;
    @SerializedName("QUANTITY")
    private int quantity;
    @SerializedName("TAX")
    private String tax;

    @SerializedName("TOTAL_PRICE")
    private String totalPrice;

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTax() {
        return tax;
    }

    public String getTotalPrice() {
        return totalPrice;
    }
}
