package com.goldtracker.remote;

import com.goldtracker.model.GoldResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface GoldApiService {

    // Giá vàng hiện tại
    @GET("v1/latest")
    Call<GoldResponse.MetalPriceResponse> getLatestPrice(
            @Query("api_key") String apiKey,
            @Query("base") String base,
            @Query("currencies") String currencies
    );

    // Giá vàng theo ngày (YYYY-MM-DD)
    @GET("v1/{date}")
    Call<GoldResponse.MetalPriceResponse> getHistoricalPrice(
            @Path("date") String date,
            @Query("api_key") String apiKey,
            @Query("base") String base,
            @Query("currencies") String currencies
    );


    // Khác domain → dùng @Url
    @GET
    Call<GoldResponse.ExchangeRateResponse> getUsdRates(
            @Url String url
    );


    // Lấy vàng XAU/USD mặc định
    default Call<GoldResponse.MetalPriceResponse> getGoldUsd(String apiKey) {
        return getLatestPrice(apiKey, "USD", "XAU");
    }

    // Lấy vàng XAU/VND trực tiếp
    default Call<GoldResponse.MetalPriceResponse> getGoldVnd(String apiKey) {
        return getLatestPrice(apiKey, "XAU", "VND");
    }

    // Lấy tỷ giá USD → VND nhanh
    default Call<GoldResponse.ExchangeRateResponse> getUsdToVnd() {
        return getUsdRates("https://open.er-api.com/v6/latest/USD");
    }
}