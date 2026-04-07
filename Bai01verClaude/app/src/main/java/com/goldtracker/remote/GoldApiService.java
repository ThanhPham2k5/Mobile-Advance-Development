package com.goldtracker.remote;

import com.goldtracker.model.GoldResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoldApiService {

    @GET("v1/latest")
    Call<GoldResponse> getRates(
            @Query("api_key") String apiKey,
            @Query("base") String base,
            @Query("currencies") String currencies
    );
}