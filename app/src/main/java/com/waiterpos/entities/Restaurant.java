package com.waiterpos.entities;

import com.google.gson.annotations.SerializedName;

public class Restaurant {
    @SerializedName("NAME")
    private String name;

    @SerializedName("TABLES")
    private int table;

    public String getName() {
        return name;
    }

    public int getTable() {
        return table;
    }
}
