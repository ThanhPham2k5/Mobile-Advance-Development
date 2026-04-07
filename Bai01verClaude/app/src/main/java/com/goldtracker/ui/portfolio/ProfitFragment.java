package com.goldtracker.ui.portfolio;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfitFragment extends Fragment {

    private EditText edtGold, edtBuyPrice;
    private Button btnCalc;
    private TextView tvResult;

    private double currentPrice = 0;
    private GoldRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profit, container, false);

        edtGold = view.findViewById(R.id.edtGold);
        edtBuyPrice = view.findViewById(R.id.edtBuyPrice);
        btnCalc = view.findViewById(R.id.btnCalc);
        tvResult = view.findViewById(R.id.tvResult);

        repo = new GoldRepository();

        loadPrice();
        setupButton();

        return view;
    }

    private void loadPrice() {
        repo.getGoldPrice().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {
                if (response.body() != null) {
                    currentPrice = response.body().getRates().get("VND");
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                tvResult.setText("Lỗi API");
            }
        });
    }

    private void setupButton() {
        btnCalc.setOnClickListener(v -> {

            String goldStr = edtGold.getText().toString().trim();
            String buyStr = edtBuyPrice.getText().toString().trim();

            if (goldStr.isEmpty() || buyStr.isEmpty()) {
                tvResult.setText("Nhập đầy đủ!");
                return;
            }

            double gold = Double.parseDouble(goldStr);
            double buyPrice = Double.parseDouble(buyStr);

            long currentTotal = Math.round(currentPrice) * Math.round(gold);
            long buyTotal = Math.round(buyPrice) * Math.round(gold);

            long profit = currentTotal - buyTotal;

            if (Math.abs(profit) < 1) profit = 0; // chống lệch 1-2 VND

            if (profit > 0) {
                tvResult.setText("Lãi: " + format(profit) + " VND");
            } else if (profit < 0) {
                tvResult.setText("Lỗ: " + format(Math.abs(profit)) + " VND");
            } else {
                tvResult.setText("Hòa vốn");
            }
        });
    }

    private String format(long number) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(number);
    }
}