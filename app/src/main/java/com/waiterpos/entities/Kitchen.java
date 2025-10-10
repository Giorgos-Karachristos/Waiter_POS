package com.waiterpos.entities;

import com.google.gson.annotations.SerializedName;

public class Kitchen {
    @SerializedName("NAME")
    private String name;
    @SerializedName("QUANTITY")
    private String quantity;
    @SerializedName("STATUS")
    private String status;

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }
}
