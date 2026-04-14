package com.goldtracker.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String API_KEY = "97f52f8f8c89c9a0a8b347c9480d7ed2";

    private static final String BASE_URL = "https://api.metalpriceapi.com/";

    private static Retrofit retrofit;
    private static GoldApiService service;
    private static OkHttpClient getClient() {

        // Logging (debug)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auto add api_key (KHÔNG cần truyền nữa)
        Interceptor apiKeyInterceptor = chain -> {
            Request original = chain.request();

            HttpUrl url = original.url();

            // Chỉ add api_key nếu gọi metalpriceapi
            if (url.host().contains("metalpriceapi.com")) {
                url = url.newBuilder()
                        .addQueryParameter("api_key", API_KEY)
                        .build();
            }

            Request request = original.newBuilder()
                    .url(url)
                    .build();

            return chain.proceed(request);
        };

        return new OkHttpClient.Builder()
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    // ===== RETROFIT SINGLETON =====
    public static GoldApiService getService() {

        if (service == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(GoldApiService.class);
        }

        return service;
    }
}