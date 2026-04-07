package com.goldtracker.ui.chart;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.remote.ApiClient;
import com.goldtracker.remote.GoldApiService;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChartFragment extends Fragment {

    private LineChart lineChart;
    private TextView tvStatus;

    private static final String API_KEY = "794300e424249b0798dd4d63e3785612";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        lineChart = view.findViewById(R.id.lineChart);
        tvStatus = view.findViewById(R.id.tvStatus);

        loadChartFromAPI();

        return view;
    }

    private void loadChartFromAPI() {
        GoldApiService api = ApiClient.getService();

        // 👉 Ưu tiên XAU (giá vàng thật)
        api.getRates(API_KEY, "XAU", "VND").enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {

                Double goldPrice = null;

                if (response.body() != null && response.body().getRates() != null) {
                    goldPrice = response.body().getRates().get("VND");
                }

                if (goldPrice != null) {
                    tvStatus.setText("Biểu đồ");
                    generateChart(goldPrice);
                } else {
                    tvStatus.setText("XAU lỗi → dùng USD fallback");
                    loadFromUSD();
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                tvStatus.setText("API lỗi → dùng USD fallback");
                loadFromUSD();
            }
        });
    }

    private void loadFromUSD() {
        GoldApiService api = ApiClient.getService();

        api.getRates(API_KEY, "USD", "VND").enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {

                if (response.body() == null || response.body().getRates() == null) {
                    generateChart(70000000); // fallback cuối
                    return;
                }

                Double rate = response.body().getRates().get("VND");

                if (rate == null) {
                    generateChart(70000000);
                    return;
                }

                // 👉 giả lập giá vàng từ USD
                double goldPrice = rate * 1900;

                generateChart(goldPrice);
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                generateChart(70000000);
            }
        });
    }
    private void generateChart(double basePrice) {

        List<Entry> entries = new ArrayList<>();
        Random random = new Random();

        lineChart.getDescription().setTextColor(Color.WHITE);

        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setTextColor(Color.WHITE);

        lineChart.getLegend().setTextColor(Color.WHITE);

        for (int i = 0; i < 7; i++) {
            float price = (float) (basePrice + random.nextInt(2000000) - 1000000);
            entries.add(new Entry(i, price));
        }

        Log.d("CHART", "entries size = " + entries.size());

        LineDataSet dataSet = new LineDataSet(entries, "Giá vàng 7 ngày");

        dataSet.setColor(Color.YELLOW);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(Color.YELLOW);

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.getDescription().setText("Biểu đồ giá vàng");
        lineChart.invalidate();
    }
}