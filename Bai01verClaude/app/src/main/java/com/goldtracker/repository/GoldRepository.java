package com.goldtracker.repository;

import com.goldtracker.model.GoldResponse;
import com.goldtracker.remote.ApiClient;

import retrofit2.Call;

public class GoldRepository {

    private static final String API_KEY = "794300e424249b0798dd4d63e3785612";
    public Call<GoldResponse> getGoldPrice() {
        return ApiClient.getService()
                .getRates(API_KEY, "XAU", "VND");
    }
}