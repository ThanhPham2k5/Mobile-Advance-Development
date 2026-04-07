package com.goldtracker;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TextView tvLivePrice, tvConvertedValue;
    private EditText edtQuantity;
    private Spinner spinnerUnit;
    private Button btnSaveHistory;
    private LineChart lineChart;
    private ListView lvHistory;

    private double currentRateVND = 0; // Tỷ giá 1 Ounce = ? VND
    private double currentConvertedVND = 0;
    private DatabaseHelper dbHelper;

    private final String API_KEY = "794300e424249b0798dd4d63e3785612";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        dbHelper = new DatabaseHelper(this);

        fetchLiveGoldPrice();
        setupChart();

        // Lắng nghe thay đổi input để tính tiền ngay lập tức
        edtQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateConversion(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSaveHistory.setOnClickListener(v -> saveToHistory());
    }

    private void initViews() {
        tvLivePrice = findViewById(R.id.tvLivePrice);
        tvConvertedValue = findViewById(R.id.tvConvertedValue);
        edtQuantity = findViewById(R.id.edtQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        btnSaveHistory = findViewById(R.id.btnSaveHistory);
        lineChart = findViewById(R.id.lineChart);
        lvHistory = findViewById(R.id.lvHistory);
    }

    private void fetchLiveGoldPrice() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.metalpriceapi.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoldApi api = retrofit.create(GoldApi.class);
        api.getLatestPrices(API_KEY, "XAU", "VND").enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRateVND = response.body().getRates().get("VND");
                    tvLivePrice.setText(String.format("%,.0f VNĐ / Ounce", currentRateVND));
                    updateHistoryList(); // Cập nhật lại lịch sử để tính ROI mới
                } else {
                    // Fallback data nếu API lỗi/hết lượt
                    currentRateVND = 60000000;
                    tvLivePrice.setText("Lỗi API - Giá Demo: 60M/Ounce");
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                currentRateVND = 60000000; // Demo data
                tvLivePrice.setText("Mất mạng - Giá Demo: 60M/Ounce");
            }
        });
    }

    private void calculateConversion() {
        if (currentRateVND == 0) return;
        String qtyStr = edtQuantity.getText().toString();
        if (qtyStr.isEmpty()) {
            tvConvertedValue.setText("0 VNĐ");
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        String unit = spinnerUnit.getSelectedItem().toString();

        // 1 Lượng ~ 1.2 Ounce, 1 Chỉ ~ 0.12 Ounce
        double multiplier = unit.equals("Ounce") ? 1.0 : (unit.equals("Lượng") ? 1.2 : 0.12);
        currentConvertedVND = qty * multiplier * currentRateVND;

        tvConvertedValue.setText(String.format("%,.0f VNĐ", currentConvertedVND));
    }

    private void saveToHistory() {
        String qtyStr = edtQuantity.getText().toString();
        if (qtyStr.isEmpty() || currentConvertedVND == 0) {
            Toast.makeText(this, "Vui lòng nhập số lượng hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String unit = spinnerUnit.getSelectedItem().toString();
        double qty = Double.parseDouble(qtyStr);

        dbHelper.addHistory(date, unit, qty, currentConvertedVND);
        Toast.makeText(this, "Đã lưu lịch sử!", Toast.LENGTH_SHORT).show();
        updateHistoryList();
    }

    private void updateHistoryList() {
        List<String> historyLogs = dbHelper.getAllHistory(currentRateVND);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyLogs);
        lvHistory.setAdapter(adapter);
    }

    private void setupChart() {
        // Mô phỏng dữ liệu 7 ngày qua
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 58000000f));
        entries.add(new Entry(2, 58500000f));
        entries.add(new Entry(3, 58200000f));
        entries.add(new Entry(4, 59000000f));
        entries.add(new Entry(5, 59500000f));
        entries.add(new Entry(6, 59300000f));
        entries.add(new Entry(7, 60000000f));

        LineDataSet dataSet = new LineDataSet(entries, "Biến động 7 ngày");
        dataSet.setColor(Color.parseColor("#FFD700"));
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(1500);
        lineChart.invalidate();
    }
}