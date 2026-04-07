package com.goldtracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoldApi {
    @GET("latest")
    Call<GoldResponse> getLatestPrices(
            @Query("api_key") String apiKey,
            @Query("base") String base,
            @Query("currencies") String currencies
    );
}
