package com.goldtracker.repository;

import android.content.Context;
import com.goldtracker.remote.ApiClient;
import com.goldtracker.remote.GoldApiService;
import com.goldtracker.model.GoldResponse;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class GoldRepository {

    private final GoldApiService api;
    private final ExecutorService executor;

    public static final double OZ_TO_GRAM = 31.1035;
    public static final double CHI_TO_GRAM = 3.75;
    public static final double LUONG_TO_GRAM = 37.5;

    public GoldRepository(Context context) {
        api = ApiClient.getService();
        executor = Executors.newSingleThreadExecutor();
    }

    // Lấy giá vàng hiện tại
    public interface GoldCallback {
        void onSuccess(List<GoldResponse.GoldPrice> prices, double worldPrice, double rate);
        void onError(Exception e);
    }

    public void getCurrentGoldPrices(GoldCallback callback) {

        executor.execute(() -> {
            try {

                Response<GoldResponse.MetalPriceResponse> metalResp =
                        api.getLatestPrice(
                                ApiClient.API_KEY,
                                "XAU",
                                "USD,VND"
                        ).execute();

                if (!metalResp.isSuccessful() || metalResp.body() == null) {
                    throw new Exception("Metal API fail: " + metalResp.code());
                }

                Map<String, Double> rates = metalResp.body().rates;

                if (rates == null || rates.get("VND") == null) {
                    throw new Exception("Missing VND rate");
                }

                // 1. Lấy giá vàng bằng USD (Đây chính là giá thế giới)
                double xauUsdPerOunce = rates.get("USD");

                // 2. Lấy giá vàng bằng VND
                double xauVndPerOunce = rates.get("VND");

                // 3. Tính tỷ giá USD/VND (Lấy giá Vàng/VND chia cho giá Vàng/USD)
                double exchangeRate = xauVndPerOunce / xauUsdPerOunce;

                // =========================
                // CONSTANTS
                // =========================
                final double OZ_TO_GRAM = 31.1035;
                final double CHI_TO_GRAM = 3.75;
                final double LUONG_TO_GRAM = 37.5;

                // =========================
                // BASE CONVERT
                // =========================
                double vndPerGram24k = xauVndPerOunce / OZ_TO_GRAM;

                List<GoldResponse.GoldPrice> prices = new ArrayList<>();

                for (GoldResponse.GoldType type : GoldResponse.GoldType.values()) {

                    double purity = type.purity;

                    double gramVnd = vndPerGram24k * purity;
                    double chiVnd = gramVnd * CHI_TO_GRAM;
                    double luongVnd = gramVnd * LUONG_TO_GRAM;

                    double ounceVnd = xauVndPerOunce * purity;

                    prices.add(new GoldResponse.GoldPrice(
                            type.displayName,
                            gramVnd,
                            chiVnd,
                            luongVnd,
                            ounceVnd
                    ));
                }

                callback.onSuccess(prices, xauUsdPerOunce, exchangeRate);

            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    // Giá 7 ngày
    public interface ChartCallback {
        void onSuccess(List<AbstractMap.SimpleEntry<String, Double>> data);
        void onError(Exception e);
    }

    public void getLast7DaysOuncePrices(ChartCallback callback) {

        executor.execute(() -> {
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat labelSdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

                Calendar cal = Calendar.getInstance();

                List<AbstractMap.SimpleEntry<String, Double>> result = new ArrayList<>();

                // ================= 6 ngày quá khứ
                for (int i = 6; i >= 1; i--) {

                    cal.setTime(new Date());
                    cal.add(Calendar.DAY_OF_YEAR, -i);

                    String dateStr = sdf.format(cal.getTime());
                    String label = labelSdf.format(cal.getTime());

                    Response<GoldResponse.MetalPriceResponse> resp =
                            api.getHistoricalPrice(
                                    dateStr,
                                    ApiClient.API_KEY,
                                    "XAU",
                                    "USD,VND"
                            ).execute();

                    if (resp.body() == null ||
                            resp.body().rates == null ||
                            resp.body().rates.get("VND") == null) {
                        continue;
                    }

                    // LẤY TRỰC TIẾP
                    double xauVnd = resp.body().rates.get("VND");

                    result.add(new AbstractMap.SimpleEntry<>(label, xauVnd));
                }

                // ================= hôm nay (latest)
                Response<GoldResponse.MetalPriceResponse> latestResp =
                        api.getLatestPrice(
                                ApiClient.API_KEY,
                                "XAU",
                                "USD,VND"
                        ).execute();

                if (latestResp.body() != null &&
                        latestResp.body().rates != null &&
                        latestResp.body().rates.get("VND") != null) {

                    double xauVnd = latestResp.body().rates.get("VND");

                    String label = labelSdf.format(new Date());

                    result.add(new AbstractMap.SimpleEntry<>(label, xauVnd));
                }

                callback.onSuccess(result);

            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

}