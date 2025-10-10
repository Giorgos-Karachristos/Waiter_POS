package com.waiterpos.api;

import com.waiterpos.entities.Category;
import com.waiterpos.entities.Item;
import com.waiterpos.entities.Kitchen;
import com.waiterpos.entities.Orders;
import com.waiterpos.entities.Register;
import com.waiterpos.entities.Restaurant;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    //MainRepository
    @GET("manager/restaurant")
    Call<List<Restaurant>> getRestaurantInfo();

    //QrScannerRepository
    @POST("waiter/isValid")
    Call<String> isValid(@Body RequestBody requestBody);

    //MenuRepository
    @GET("cashier/menu/getCategoryList")
    Call<List<Category>> getCategoryList();

    @GET("cashier/menu/getItemsList")
    Call<List<Item>> getItemsListFromCategory(@Query("category") String category);

    @POST("cashier/menu/insertOrder")
    Call<String> insertOrderToKitchen(@Body RequestBody requestBody);

    @POST("cashier/menu/insertKitchen")
    Call<String> insertItemToOrder(@Body RequestBody requestBody);

    //OrdersRepository
    @GET("cashier/order/getActiveOrdersList")
    Call<List<Orders>> getActiveOrdersList();

    @GET("cashier/order/getKitchenList")
    Call<List<Kitchen>> getKitchenList(@Query("orderID") String category);

    @POST("cashier/order/voidKitchen")
    Call<String> voidKitchen(@Body RequestBody requestBody);

    @POST("cashier/order/cancelOrder")
    Call<String> cancelOrder(@Body RequestBody requestBody);

    //RegisterRepository
    @GET("cashier/register/getItemsList")
    Call<List<Register>> getRegisterList(@Query("orderID") String category);

    @POST("cashier/register/completeOrder")
    Call<String> completeOrder(@Body RequestBody requestBody);
}