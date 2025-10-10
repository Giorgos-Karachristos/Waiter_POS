package com.waiterpos.entities;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("NAME")
    private String name;
    @SerializedName("COLOR")
    private String color;
    @SerializedName("TAX")
    private String tax;

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getTax() {
        return tax;
    }
}
