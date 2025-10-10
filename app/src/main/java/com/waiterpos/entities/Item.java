package com.waiterpos.entities;

import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("NAME")
    private String name;
    @SerializedName("COLOR")
    private String color;
    @SerializedName("PRICE")
    private String price;
    @SerializedName("ITEM_ID")
    private String itemID;
    @SerializedName("DESCRIPTION")
    private String description;

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getPrice() {
        return price;
    }

    public String getItemID() {
        return itemID;
    }

    public String getDescription() {
        return description;
    }
}

