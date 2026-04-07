package com.goldtracker.ui.converter;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.goldtracker.Data.History;
import com.goldtracker.MainActivity;
import com.goldtracker.R;
import com.goldtracker.model.GoldResponse;
import com.goldtracker.repository.GoldRepository;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.*;

public class ConverterFragment extends Fragment {

    private EditText edtGold;
    private TextView tvResult;
    private Button btnConvert;

    private double currentPrice = 0;
    private GoldRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_converter, container, false);

        edtGold = view.findViewById(R.id.edtGold);
        tvResult = view.findViewById(R.id.tvResult);
        btnConvert = view.findViewById(R.id.btnConvert);

        repo = new GoldRepository();

        loadPrice();
        setupConvertButton();

        return view;
    }

    private void loadPrice() {
        repo.getGoldPrice().enqueue(new Callback<GoldResponse>() {
            @Override
            public void onResponse(Call<GoldResponse> call, Response<GoldResponse> response) {
                if (response.body() != null && response.body().getRates() != null) {
                    currentPrice = response.body().getRates().get("VND");
                }
            }

            @Override
            public void onFailure(Call<GoldResponse> call, Throwable t) {
                tvResult.setText("Lỗi API");
            }
        });
    }

    private void setupConvertButton() {
        btnConvert.setOnClickListener(v -> {

            String input = edtGold.getText().toString().trim();

            if (input.isEmpty()) {
                tvResult.setText("Nhập số vàng!");
                return;
            }

            if (currentPrice == 0) {
                tvResult.setText("Chưa có giá vàng!");
                return;
            }

            double gold = Double.parseDouble(input);
            double result = gold * currentPrice;

            tvResult.setText(format(Math.round(result)) + " VND");

            History history = new History();
            history.goldAmount = gold;
            history.price = currentPrice;
            history.result = result;
            history.time = new java.text.SimpleDateFormat("HH:mm dd/MM")
                    .format(new java.util.Date());

            MainActivity.db.historyDao().insert(history);
        });
    }

    private String format(long number) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(number);
    }
}